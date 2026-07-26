import Combine
import Foundation

enum AppScreen: String { case home, map, region, topic, lesson, revision, review, challenge, search, starred, settings
    var isTopLevel: Bool { [.home, .map, .search, .starred, .settings].contains(self) }
}
struct NavState {
    var screen: AppScreen = .home
    var categoryId, nodeId, lessonId, quizId, challengeId: String?
    var aiOpen = false; var query = ""; var tagFilter = "All"; var backStack: [NavState] = []
    func withoutStack() -> NavState { var copy = self; copy.backStack = []; return copy }
}
enum QuizPhase { case question, feedback, done }
struct QuizUIState {
    let quizId: String; var index = 0; var phase: QuizPhase = .question
    var answers: [String: UserAnswer] = [:]; var selfAssessments: [String: Bool] = [:]
    var lastCorrect: Bool?; var score: QuizScore?; var recorded: QuizRecordResult?
}
struct ReviewUIState {
    let recallItemIds: [String]; var index = 0; var answer = ""; var revealed = false; var lastRating: ReviewRating?
    var currentId: String? { recallItemIds.indices.contains(index) ? recallItemIds[index] : nil }
    var isComplete: Bool { index >= recallItemIds.count }
}
enum ContentLoadState { case loading, success(LoadedContent), failure(ContentFailure) }

@MainActor
final class AppModel: ObservableObject {
    @Published private(set) var loadState: ContentLoadState = .loading
    @Published var nav = NavState()
    @Published var quiz: QuizUIState?
    @Published var review: ReviewUIState?
    let progressStore: ProgressStore
    private let repository: ContentRepository?

    init(bundle: Bundle = .main, progressStore: ProgressStore? = nil) {
        self.progressStore = progressStore ?? ProgressStore()
        self.repository = try? ContentRepository(source: .bundled(bundle))
        loadContent()
    }
    init(repository: ContentRepository, progressStore: ProgressStore) {
        self.repository = repository; self.progressStore = progressStore; loadContent()
    }
    var content: LoadedContent? { if case .success(let value) = loadState { value } else { nil } }
    var progress: LearnerProgress { progressStore.progress }

    func loadContent() {
        loadState = .loading
        guard let repository else {
            loadState = .failure(ContentFailure(kind: .missingContent, message: "The bundled curriculum could not be located.")); return
        }
        Task { @MainActor in
            await Task.yield()
            do { loadState = .success(try repository.load()) }
            catch let failure as ContentFailure { loadState = .failure(failure) }
            catch { loadState = .failure(ContentFailure(kind: .unknown, message: error.localizedDescription)) }
        }
    }

    func go(to screen: AppScreen) { nav.screen = screen; nav.aiOpen = false }
    private func push(_ mutate: (inout NavState) -> Void) {
        var next = nav; let previous = nav.withoutStack(); mutate(&next); next.backStack = nav.backStack + [previous]; nav = next
    }
    func back() {
        if let previous = nav.backStack.last { var p = previous; p.backStack = Array(nav.backStack.dropLast()); nav = p }
        else { nav.screen = .home }
    }
    func openCategory(_ id: String) { push { $0.screen = .region; $0.categoryId = id; $0.aiOpen = false } }
    func openNode(_ id: String) {
        guard let node = content?.roadmapNodesById[id], !ProgressionPolicy.isPlanned(node) else { return }
        switch node.type {
        case .lesson, .start: if let lesson = node.lessonId { openTopic(lesson, nodeId: id, categoryId: node.categoryId) }
        case .checkpoint, .boss: if let quiz = node.quizId { startQuiz(quiz) }
        case .levelPreview: break
        }
    }
    func openTopic(_ lessonId: String, nodeId: String?, categoryId: String?) {
        push { $0.screen = .topic; $0.lessonId = lessonId; $0.nodeId = nodeId; $0.categoryId = categoryId; $0.aiOpen = false }
    }
    func openLesson() { if let id = nav.nodeId { progressStore.markNodeRead(id) }; push { $0.screen = .lesson } }
    func openChallenge(_ id: String) { push { $0.screen = .challenge; $0.challengeId = id; $0.aiOpen = false } }
    func routeSearch(_ doc: SearchDocument) {
        guard let content else { return }
        switch SearchRouter.route(content, doc) {
        case .lesson(let id, let node, let category): openTopic(id, nodeId: node, categoryId: category)
        case .quiz(let id): startQuiz(id)
        case .challenge(let id): openChallenge(id)
        case .category(let id): openCategory(id)
        case .none: break
        }
    }

    func startQuiz(_ id: String) { quiz = QuizUIState(quizId: id); push { $0.screen = .revision; $0.quizId = id; $0.aiOpen = false } }
    func setAnswer(_ answer: UserAnswer, questionId: String) { quiz?.answers[questionId] = answer }
    func submitCurrentQuestion() {
        guard var state = quiz, let q = content?.quiz(state.quizId)?.questions[safe: state.index] else { return }
        state.phase = .feedback; state.lastCorrect = QuizEvaluator.requiresSelfAssessment(q) ? nil : QuizEvaluator.isCorrect(q, state.answers[q.id] ?? .none); quiz = state
    }
    func assessCurrentQuestion(_ correct: Bool) {
        guard var state = quiz, state.phase == .feedback, let q = content?.quiz(state.quizId)?.questions[safe: state.index], QuizEvaluator.requiresSelfAssessment(q) else { return }
        state.selfAssessments[q.id] = correct; state.lastCorrect = correct; quiz = state
    }
    func nextQuestion() {
        guard var state = quiz, let quizData = content?.quiz(state.quizId) else { return }
        if state.index + 1 < quizData.questions.count { state.index += 1; state.phase = .question; state.lastCorrect = nil; quiz = state }
        else {
            let score = QuizEvaluator.score(quizData, answers: state.answers, selfAssessments: state.selfAssessments)
            state.phase = .done; state.score = score
            let node = content.map { ProgressionPolicy.nodeCompleted(by: quizData, in: $0.roadmap) } ?? nil
            state.recorded = progressStore.recordQuiz(quizId: quizData.id, nodeId: node, score: score.fraction, passingScore: quizData.passingScore, rewardXP: quizData.rewards.xp, maxStars: quizData.rewards.stars)
            quiz = state
        }
    }
    func retryQuiz() { if let id = quiz?.quizId { quiz = QuizUIState(quizId: id) } }
    func exitQuiz() { quiz = nil; back() }

    func rateRecall(lessonId: String, recallId: String, rating: ReviewRating) {
        guard let item = content?.recallItem(recallId), item.lesson.id == lessonId else { return }
        let state = SpacedRepetitionPolicy.next(id: recallId, previous: progress.reviewStates[recallId], rating: rating, authoredIntervals: item.lesson.revision.reviewIntervalsDays, now: .now)
        progressStore.saveReview(state)
    }
    func startDailyReview() {
        guard let content else { return }
        let ids = progress.reviewStates.values.filter { $0.isDue(.now) && content.recallItem($0.recallItemId) != nil }.sorted { $0.dueAt < $1.dueAt }.prefix(20).map(\.recallItemId)
        review = ReviewUIState(recallItemIds: ids); push { $0.screen = .review; $0.aiOpen = false }
    }
    func revealReview() { if review?.answer.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false { review?.revealed = true } }
    func rateCurrentReview(_ rating: ReviewRating) {
        guard var state = review, state.revealed, let item = content?.recallItem(state.currentId) else { return }
        let next = SpacedRepetitionPolicy.next(id: item.id, previous: progress.reviewStates[item.id], rating: rating, authoredIntervals: item.lesson.revision.reviewIntervalsDays, now: .now)
        progressStore.saveReview(next); state.index += 1; state.answer = ""; state.revealed = false; state.lastRating = rating; review = state
    }
    func exitReview() { review = nil; back() }
}

extension Collection { subscript(safe index: Index) -> Element? { indices.contains(index) ? self[index] : nil } }
