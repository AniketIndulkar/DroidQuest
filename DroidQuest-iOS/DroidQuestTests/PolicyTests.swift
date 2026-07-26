import Foundation
import XCTest
@testable import DroidQuest

final class PolicyTests: XCTestCase {
    func testBundledSourceOfTruthLoadsAndVerifies() throws {
        let repoRoot = URL(fileURLWithPath: #filePath).deletingLastPathComponent().deletingLastPathComponent().deletingLastPathComponent()
        let content = try ContentRepository(source: ContentSource(root: repoRoot.appendingPathComponent("data"))).load()
        XCTAssertEqual(content.categories.count, 12)
        XCTAssertEqual(content.lessonsById.count, 302)
        XCTAssertEqual(content.quizzesById.count, 361)
        XCTAssertEqual(content.challengesById.count, 302)
        XCTAssertEqual(content.glossary.count, 313)
        XCTAssertFalse(content.search.documents.isEmpty)
        XCTAssertFalse(content.roadmap.nodes.isEmpty)
    }

    func testApplicationBundleContainsCompleteCurriculum() throws {
        let content = try ContentRepository(source: .bundled()).load()
        XCTAssertEqual(content.index.contentRevision, 16)
        XCTAssertEqual(content.lessonsById.count, content.index.counts.lessons)
        XCTAssertEqual(content.quizzesById.count, content.index.counts.quizzes)
    }

    func testRewardIsIdempotentAndStarsScale() {
        XCTAssertEqual(RewardPolicy.stars(score: 0.8, maxStars: 3, passingScore: 0.8), 3)
        let first = RewardPolicy.evaluate(score: 1, passingScore: 0.8, rewardXP: 70, maxStars: 3, alreadyPassed: false)
        XCTAssertEqual(first, RewardOutcome(passed: true, starsEarned: 3, xpAwarded: 70, starsAwarded: 3))
        let repeatPass = RewardPolicy.evaluate(score: 1, passingScore: 0.8, rewardXP: 70, maxStars: 3, alreadyPassed: true)
        XCTAssertEqual(repeatPass.xpAwarded, 0); XCTAssertEqual(repeatPass.starsAwarded, 0)
    }

    func testEveryQuizQuestionTypeGrades() {
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.singleChoice, answer: .string("A"), options: [.string("A"), .string("B")] ), .text(" a ")))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.multipleChoice, answer: .array([.string("A"), .string("B")])), .choices(["b", "a"])))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.trueFalse, answer: .bool(true)), .bool(true)))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.fillBlank, answer: .string("State Flow")), .text(" state   flow ")))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.orderSteps, answer: .array([.string("one"), .string("two")])), .choices(["one", "two"])))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.matchPairs, answer: .object(["A":.string("1"), "B":.string("2")])), .pairs(["A":"1", "B":"2"])))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.codeOutput, answer: .string("42")), .text("42")))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.spotBug, answer: .string("race")), .text("race")))
        XCTAssertTrue(QuizEvaluator.isCorrect(question(.shortAnswer, answer: .string("model")), .text("model")))
    }

    func testSpacedRepetitionUsesAuthoredIntervals() {
        let now = Date(timeIntervalSince1970: 1_000)
        let good = SpacedRepetitionPolicy.next(id: "r1", previous: nil, rating: .good, authoredIntervals: [1, 7, 21], now: now)
        XCTAssertEqual(good.intervalDays, 1); XCTAssertEqual(good.repetitions, 1)
        let easy = SpacedRepetitionPolicy.next(id: "r1", previous: good, rating: .easy, authoredIntervals: [1, 7, 21], now: now)
        XCTAssertEqual(easy.intervalDays, 21)
        let again = SpacedRepetitionPolicy.next(id: "r1", previous: good, rating: .again, authoredIntervals: [1, 7, 21], now: now)
        XCTAssertEqual(again.intervalDays, 0); XCTAssertEqual(again.lapses, 1); XCTAssertEqual(again.dueAt.timeIntervalSince(now), 600)
    }

    @MainActor func testProgressPersistenceAndFirstPassRewards() throws {
        let suite = "DroidQuestTests.\(UUID())"; let defaults = try XCTUnwrap(UserDefaults(suiteName: suite)); defer { defaults.removePersistentDomain(forName: suite) }
        let store = ProgressStore(defaults: defaults)
        let first = store.recordQuiz(quizId: "q", nodeId: "n", score: 1, passingScore: 0.8, rewardXP: 10, maxStars: 3)
        let second = store.recordQuiz(quizId: "q", nodeId: "n", score: 1, passingScore: 0.8, rewardXP: 10, maxStars: 3)
        XCTAssertTrue(first.firstPass); XCTAssertFalse(second.firstPass); XCTAssertEqual(store.progress.totalXp, 10); XCTAssertEqual(store.progress.quizAttempts["q"], 2)
        XCTAssertEqual(ProgressStore(defaults: defaults).progress.completedNodeIds, ["n"])
    }

    private func question(_ type: QuestionType, answer: JSONValue, options: [JSONValue]? = nil) -> Question {
        Question(id: UUID().uuidString, prompt: "Prompt", type: type, difficulty: "easy", options: options, answer: answer, acceptedAnswers: nil, explanation: "Why", lessonId: "lesson")
    }
}
