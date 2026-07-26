import Combine
import Foundation

struct LearnerSettings: Codable, Equatable, Sendable {
    var githubConnected = false
    var notifications = true
    var sound = true
}

struct LearnerProgress: Codable, Equatable, Sendable {
    var completedNodeIds: Set<String> = []
    var starredLessonIds: Set<String> = []
    var completedChallengeIds: Set<String> = []
    var passedQuizIds: Set<String> = []
    var readNodeIds: Set<String> = []
    var bestQuizScore: [String: Double] = [:]
    var quizAttempts: [String: Int] = [:]
    var reviewStates: [String: ReviewState] = [:]
    var totalXp = 0
    var totalStars = 0
    var settings = LearnerSettings()

    func reviewsDue(_ now: Date = .now) -> Int { reviewStates.values.filter { $0.isDue(now) }.count }
}

struct QuizRecordResult: Equatable {
    let outcome: RewardOutcome; let completedNode: String?; let firstPass: Bool
}

@MainActor
final class ProgressStore: ObservableObject {
    @Published private(set) var progress: LearnerProgress
    private let defaults: UserDefaults
    private let key = "droidquest.learner-progress.v1"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        if let data = defaults.data(forKey: key), let decoded = try? JSONDecoder().decode(LearnerProgress.self, from: data) { progress = decoded }
        else { progress = LearnerProgress() }
    }

    func toggleStar(_ lessonId: String) {
        if progress.starredLessonIds.contains(lessonId) { progress.starredLessonIds.remove(lessonId) }
        else { progress.starredLessonIds.insert(lessonId) }
        save()
    }
    func markNodeRead(_ id: String) { progress.readNodeIds.insert(id); save() }
    @discardableResult
    func recordQuiz(quizId: String, nodeId: String?, score: Double, passingScore: Double, rewardXP: Int, maxStars: Int) -> QuizRecordResult {
        let alreadyPassed = progress.passedQuizIds.contains(quizId)
        let outcome = RewardPolicy.evaluate(score: score, passingScore: passingScore, rewardXP: rewardXP, maxStars: maxStars, alreadyPassed: alreadyPassed)
        let firstPass = outcome.passed && !alreadyPassed
        progress.quizAttempts[quizId, default: 0] += 1
        progress.bestQuizScore[quizId] = max(score, progress.bestQuizScore[quizId] ?? 0)
        if outcome.passed { progress.passedQuizIds.insert(quizId) }
        if firstPass {
            progress.totalXp += outcome.xpAwarded; progress.totalStars += outcome.starsAwarded
            if let nodeId { progress.completedNodeIds.insert(nodeId) }
        }
        save()
        return QuizRecordResult(outcome: outcome, completedNode: firstPass ? nodeId : nil, firstPass: firstPass)
    }
    func completeChallenge(_ challenge: Challenge) {
        if progress.completedChallengeIds.insert(challenge.id).inserted {
            progress.totalXp += challenge.rewards.xp; progress.totalStars += challenge.rewards.stars; save()
        }
    }
    func saveReview(_ state: ReviewState) { progress.reviewStates[state.recallItemId] = state; save() }
    func setGithub(_ value: Bool) { progress.settings.githubConnected = value; save() }
    func setNotifications(_ value: Bool) { progress.settings.notifications = value; save() }
    func setSound(_ value: Bool) { progress.settings.sound = value; save() }

    private func save() { if let data = try? JSONEncoder().encode(progress) { defaults.set(data, forKey: key) } }
}
