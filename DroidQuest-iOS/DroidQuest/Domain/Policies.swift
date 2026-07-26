import Foundation

enum NodeProgress { case locked, available, completed }
enum ProgressionPolicy {
    static func isPlanned(_ node: RoadmapNode) -> Bool { node.type == .levelPreview || node.status == .planned }
    static func prerequisitesMet(_ node: RoadmapNode, _ completed: Set<String>) -> Bool { node.unlockPrerequisites.allSatisfy(completed.contains) }
    static func canStart(_ node: RoadmapNode, _ completed: Set<String>) -> Bool { !isPlanned(node) && !completed.contains(node.id) && prerequisitesMet(node, completed) }
    static func progress(of node: RoadmapNode, completed: Set<String>) -> NodeProgress {
        completed.contains(node.id) ? .completed : (canStart(node, completed) ? .available : .locked)
    }
    static func availableNodes(_ graph: RoadmapGraph, _ completed: Set<String>) -> [RoadmapNode] {
        let byId = Dictionary(uniqueKeysWithValues: graph.nodes.map { ($0.id, $0) })
        let order = (graph.topologicalOrder?.isEmpty == false) ? graph.topologicalOrder! : graph.nodes.map(\.id)
        return order.compactMap { byId[$0] }.filter { canStart($0, completed) }
    }
    static func nextNode(_ graph: RoadmapGraph, _ completed: Set<String>) -> RoadmapNode? { availableNodes(graph, completed).first }
    static func nodeCompleted(by quiz: Quiz, in graph: RoadmapGraph) -> String? {
        if quiz.kind == .lesson, let lesson = quiz.linkedLessonIds.first {
            return graph.nodes.first { ($0.type == .lesson || $0.type == .start) && $0.lessonId == lesson }?.id
                ?? graph.nodes.first { $0.quizId == quiz.id }?.id
        }
        return graph.nodes.first { $0.quizId == quiz.id }?.id
    }
    static func isCategoryUnlocked(_ graph: RoadmapGraph, categoryId: String, completed: Set<String>) -> Bool {
        guard let cat = graph.categories?.first(where: { $0.id == categoryId }) else { return false }
        let byId = Dictionary(uniqueKeysWithValues: graph.nodes.map { ($0.id, $0) })
        return (cat.startNodeIds + cat.previewNodeIds).contains { id in
            guard let node = byId[id] else { return false }
            return completed.contains(node.id) || canStart(node, completed) || prerequisitesMet(node, completed)
        }
    }
}

enum UserAnswer: Equatable {
    case text(String), bool(Bool), choices([String]), pairs([String: String]), none
}
struct QuizScore: Equatable { let correct, total: Int; let fraction: Double; let passed: Bool }
enum QuizEvaluator {
    static func normalize(_ value: String) -> String {
        value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            .split(whereSeparator: \.isWhitespace).joined(separator: " ")
    }
    static func options(_ question: Question) -> [String] { question.options?.map(\.text) ?? [] }
    static func requiresSelfAssessment(_ question: Question) -> Bool { question.type == .shortAnswer || question.type == .spotBug }
    static func modelAnswer(_ question: Question) -> String {
        switch question.type {
        case .multipleChoice: return question.answer.strings.map { "• \($0)" }.joined(separator: "\n")
        case .orderSteps: return question.answer.strings.enumerated().map { "\($0 + 1). \($1)" }.joined(separator: "\n")
        case .matchPairs: return question.answer.stringMap.map { "\($0.key) → \($0.value)" }.joined(separator: "\n")
        case .trueFalse: return question.answer.boolean ? "True" : "False"
        default: return question.answer.text
        }
    }
    static func isCorrect(_ question: Question, _ answer: UserAnswer) -> Bool {
        switch question.type {
        case .singleChoice:
            if case .text(let value) = answer { return normalize(value) == normalize(question.answer.text) }
        case .multipleChoice:
            if case .choices(let values) = answer { return Set(values.map(normalize)) == Set(question.answer.strings.map(normalize)) }
        case .trueFalse:
            if case .bool(let value) = answer { return value == question.answer.boolean }
        case .orderSteps:
            if case .choices(let values) = answer { return values.map(normalize) == question.answer.strings.map(normalize) }
        case .matchPairs:
            if case .pairs(let values) = answer {
                let expected = question.answer.stringMap
                return Set(values.keys).isSuperset(of: Set(expected.keys)) && expected.allSatisfy { key, value in values[key].map(normalize) == normalize(value) }
            }
        case .fillBlank, .shortAnswer, .spotBug, .codeOutput:
            if case .text(let value) = answer {
                let accepted = Set(([question.answer.text] + (question.acceptedAnswers ?? [])).map(normalize))
                return accepted.contains(normalize(value))
            }
        }
        return false
    }
    static func score(_ quiz: Quiz, answers: [String: UserAnswer], selfAssessments: [String: Bool] = [:]) -> QuizScore {
        let correct = quiz.questions.filter { q in
            requiresSelfAssessment(q) ? selfAssessments[q.id] == true : isCorrect(q, answers[q.id] ?? .none)
        }.count
        let fraction = quiz.questions.isEmpty ? 0 : Double(correct) / Double(quiz.questions.count)
        return QuizScore(correct: correct, total: quiz.questions.count, fraction: fraction, passed: fraction >= quiz.passingScore)
    }
}

struct RewardOutcome: Equatable { let passed: Bool; let starsEarned, xpAwarded, starsAwarded: Int }
enum RewardPolicy {
    static func stars(score: Double, maxStars: Int, passingScore: Double) -> Int {
        score < passingScore ? 0 : max(1, min(Int(ceil(score * Double(maxStars))), maxStars))
    }
    static func evaluate(score: Double, passingScore: Double, rewardXP: Int, maxStars: Int, alreadyPassed: Bool) -> RewardOutcome {
        let passed = score >= passingScore
        let stars = stars(score: score, maxStars: maxStars, passingScore: passingScore)
        let firstPass = passed && !alreadyPassed
        return RewardOutcome(passed: passed, starsEarned: stars, xpAwarded: firstPass ? rewardXP : 0, starsAwarded: firstPass ? stars : 0)
    }
}

enum ReviewRating: String, Codable, CaseIterable, Sendable { case again, hard, good, easy }
struct ReviewState: Codable, Equatable, Sendable {
    let recallItemId: String; let dueAt: Date; let intervalDays, repetitions, lapses: Int
    let lastReviewedAt: Date; let lastRating: ReviewRating
    func isDue(_ now: Date) -> Bool { dueAt <= now }
}
enum SpacedRepetitionPolicy {
    static func next(id: String, previous: ReviewState?, rating: ReviewRating, authoredIntervals: [Int], now: Date) -> ReviewState {
        let intervals = Array(Set(authoredIntervals.filter { $0 > 0 })).sorted().isEmpty ? [1, 7, 21] : Array(Set(authoredIntervals.filter { $0 > 0 })).sorted()
        let current = previous?.intervalDays ?? 0
        let interval: Int
        switch rating {
        case .again: interval = 0
        case .hard: interval = current <= 1 ? 1 : max(1, current / 2)
        case .good: interval = successfulInterval(current, intervals, skip: 0)
        case .easy: interval = successfulInterval(current, intervals, skip: 1)
        }
        let bounded = min(interval, 365)
        let due = rating == .again ? now.addingTimeInterval(600) : now.addingTimeInterval(Double(bounded) * 86_400)
        return ReviewState(recallItemId: id, dueAt: due, intervalDays: bounded,
            repetitions: (previous?.repetitions ?? 0) + (rating == .again ? 0 : 1),
            lapses: (previous?.lapses ?? 0) + (rating == .again && previous != nil ? 1 : 0),
            lastReviewedAt: now, lastRating: rating)
    }
    private static func successfulInterval(_ current: Int, _ values: [Int], skip: Int) -> Int {
        if let index = values.firstIndex(where: { $0 > current }) { return values[min(index + skip, values.count - 1)] }
        return max(values.last!, max(1, current) * (skip == 0 ? 2 : 3))
    }
}

enum SearchRoute: Equatable { case lesson(String, String?, String?), quiz(String), challenge(String), category(String), none }
enum SearchRouter {
    static func route(_ content: LoadedContent, _ doc: SearchDocument) -> SearchRoute {
        switch doc.type {
        case "lesson": return .lesson(doc.id, nodeForLesson(content, doc.id), doc.categoryId)
        case "quiz": return .quiz(doc.id)
        case "challenge": return .challenge(doc.id)
        case "category": return .category(doc.categoryId ?? doc.id)
        case "glossary":
            if let entry = content.glossary.first(where: { $0.id == doc.id }), let id = entry.relatedLessonIds.first, let lesson = content.lesson(id) {
                return .lesson(lesson.id, nodeForLesson(content, lesson.id), lesson.categoryId)
            }
            return doc.categoryId.map(SearchRoute.category) ?? .none
        default: return .none
        }
    }
    private static func nodeForLesson(_ content: LoadedContent, _ id: String) -> String? { content.roadmap.nodes.first { $0.lessonId == id }?.id }
}

struct CategoryProgress { let completed, total, starsEarned, percent: Int }
enum UIDerive {
    static func categoryNodes(_ content: LoadedContent, _ id: String) -> [RoadmapNode] { content.roadmap.nodes.filter { $0.categoryId == id && $0.type != .levelPreview } }
    static func categoryProgress(_ content: LoadedContent, _ progress: LearnerProgress, _ id: String) -> CategoryProgress {
        let nodes = categoryNodes(content, id); let done = nodes.filter { progress.completedNodeIds.contains($0.id) }
        return CategoryProgress(completed: done.count, total: nodes.count, starsEarned: done.reduce(0) { $0 + $1.rewards.stars }, percent: nodes.isEmpty ? 0 : Int((Double(done.count) / Double(nodes.count) * 100).rounded()))
    }
    static func nextNode(_ content: LoadedContent, _ progress: LearnerProgress) -> RoadmapNode? { ProgressionPolicy.nextNode(content.roadmap, progress.completedNodeIds) }
    static func currentLevel(_ content: LoadedContent, _ progress: LearnerProgress) -> Int {
        if let next = nextNode(content, progress), let order = content.category(next.categoryId)?.order { return order }
        let complete = content.categories.filter { $0.status != .planned && categoryProgress(content, progress, $0.id).completed == categoryProgress(content, progress, $0.id).total }.count
        return min(complete + 1, content.categories.count)
    }
    static func totalStars(_ content: LoadedContent, _ progress: LearnerProgress) -> Int { content.roadmap.nodes.filter { progress.completedNodeIds.contains($0.id) }.reduce(0) { $0 + $1.rewards.stars } }
    static func maxStars(_ content: LoadedContent) -> Int { content.roadmap.nodes.filter { $0.type != .levelPreview }.reduce(0) { $0 + $1.rewards.stars } }
}
