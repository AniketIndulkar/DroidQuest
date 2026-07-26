import Foundation

// MARK: - Flexible JSON

enum JSONValue: Codable, Hashable, Sendable {
    case string(String), number(Double), bool(Bool), array([JSONValue]), object([String: JSONValue]), null

    init(from decoder: Decoder) throws {
        let c = try decoder.singleValueContainer()
        if c.decodeNil() { self = .null }
        else if let v = try? c.decode(Bool.self) { self = .bool(v) }
        else if let v = try? c.decode(Double.self) { self = .number(v) }
        else if let v = try? c.decode(String.self) { self = .string(v) }
        else if let v = try? c.decode([JSONValue].self) { self = .array(v) }
        else { self = .object(try c.decode([String: JSONValue].self)) }
    }

    func encode(to encoder: Encoder) throws {
        var c = encoder.singleValueContainer()
        switch self {
        case .string(let v): try c.encode(v)
        case .number(let v): try c.encode(v)
        case .bool(let v): try c.encode(v)
        case .array(let v): try c.encode(v)
        case .object(let v): try c.encode(v)
        case .null: try c.encodeNil()
        }
    }

    var text: String {
        switch self {
        case .string(let v): return v
        case .number(let v): return v.rounded() == v ? String(Int(v)) : String(v)
        case .bool(let v): return String(v)
        case .null: return ""
        case .array(let v): return v.map(\.text).joined(separator: ", ")
        case .object(let v): return v.map { "\($0.key): \($0.value.text)" }.joined(separator: ", ")
        }
    }

    var strings: [String] { if case .array(let v) = self { return v.map(\.text) }; return [] }
    var stringMap: [String: String] {
        if case .object(let v) = self { return v.mapValues(\.text) }
        return [:]
    }
    var boolean: Bool { if case .bool(let v) = self { return v }; return text.lowercased() == "true" }
}

enum CategoryStatus: String, Codable, Sendable { case planned, inProgress = "in_progress", complete }
enum RoadmapNodeType: String, Codable, Sendable { case start, lesson, checkpoint, boss, levelPreview = "level_preview" }
enum NodeStatus: String, Codable, Sendable { case available, planned }
enum QuizKind: String, Codable, Sendable { case lesson, weekCheckpoint = "week_checkpoint", levelCheckpoint = "level_checkpoint", boss }
enum QuestionType: String, Codable, CaseIterable, Sendable {
    case singleChoice = "single_choice", multipleChoice = "multiple_choice", trueFalse = "true_false"
    case fillBlank = "fill_blank", orderSteps = "order_steps", matchPairs = "match_pairs"
    case codeOutput = "code_output", spotBug = "spot_bug", shortAnswer = "short_answer"
}
enum CalloutTone: String, Codable, Sendable { case note, remember, warning }

struct Curriculum: Codable, Sendable {
    let id, title, description, version: String
    let contentRevision: Int
    let releasedAt: String
    let categoryIds: [String]
    let roadmapId: String
    let glossaryIds: [String]
    let authoredWeeks: [AuthoredWeek]
    let minimumAppContentApi: Int
}
struct AuthoredWeek: Codable, Sendable {
    let id: String; let number: Int; let categoryId, title, status: String
    let lessonIds: [String]; let checkpointQuizId: String?
}

struct WeekRange: Codable, Sendable { let start, end: Int }
struct CategoryProject: Codable, Sendable { let title, summary: String }
struct CategoryTheme: Codable, Sendable { let color, icon, mapMood: String }
struct Category: Codable, Identifiable, Sendable {
    let id, title: String; let order: Int; let description, difficulty: String
    let status: CategoryStatus; let weekRange: WeekRange
    let unlockPrerequisites, tags: [String]; let estimatedLearningMinutes: Int
    let lessonIds: [String]; let checkpointQuizId, roadmapStartNodeId: String?
    let plannedTopics: [String]; let project: CategoryProject; let theme: CategoryTheme; let version: String
}

struct Lesson: Codable, Identifiable, Sendable {
    let id, title, categoryId: String; let week: Int; let difficulty: String
    let prerequisites, tags: [String]; let estimatedLearningMinutes: Int
    let revealStages: RevealStages; let quizId, challengeId: String
    let revision: Revision; let sourceRefs: [String]; let version: String
}
struct RevealStages: Codable, Sendable {
    let scout: Scout; let learn: Learn; let inspect: Inspect
    let trapCheck: [Trap]; let challengeIntro: ChallengeIntro; let recall: [Recall]
    enum CodingKeys: String, CodingKey { case scout, learn, inspect, trapCheck = "trap_check", challengeIntro = "challenge_intro", recall }
}
struct Scout: Codable, Sendable { let purpose, realWorldUse, outcome: String }
struct Learn: Codable, Sendable { let estimatedMinutes: Int; let sections: [LearnSection]; let furtherReading: [FurtherReading] }
struct LearnSection: Codable, Identifiable, Sendable { let id, title: String; let blocks: [LearnBlock] }
struct FurtherReading: Codable, Identifiable, Sendable {
    var id: String { url }; let title, publisher, resourceType, url, whyRead: String
}
struct Inspect: Codable, Sendable { let title, language, code: String; let walkthrough: [String]; let expectedOutput: String }
struct Trap: Codable, Identifiable, Sendable { var id: String { mistake }; let mistake, why, fix: String }
struct ChallengeIntro: Codable, Sendable { let task, successLooksLike: String }
struct Recall: Codable, Identifiable, Sendable { let id, prompt, answer: String }
struct Revision: Codable, Sendable {
    let objectives: [String]; let reviewIntervalsDays: [Int]; let masteryThreshold: Double; let xp, starsAvailable: Int
}

enum LearnBlock: Codable, Identifiable, Sendable {
    case paragraph(text: String)
    case code(language: String, code: String, caption: String)
    case callout(tone: CalloutTone, title: String, text: String)
    case flow(title: String, steps: [FlowStep])
    case table(title: String, columns: [String], rows: [[String]])
    case list(title: String, items: [String])

    var id: String {
        switch self {
        case .paragraph(let text): return "p-\(text.hashValue)"
        case .code(_, let code, _): return "c-\(code.hashValue)"
        case .callout(_, let title, _), .flow(let title, _), .table(let title, _, _), .list(let title, _): return "b-\(title.hashValue)"
        }
    }
    private enum Keys: String, CodingKey { case type, text, language, code, caption, tone, title, steps, columns, rows, items }
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: Keys.self)
        switch try c.decode(String.self, forKey: .type) {
        case "paragraph": self = .paragraph(text: try c.decode(String.self, forKey: .text))
        case "code": self = .code(language: try c.decode(String.self, forKey: .language), code: try c.decode(String.self, forKey: .code), caption: try c.decode(String.self, forKey: .caption))
        case "callout": self = .callout(tone: try c.decode(CalloutTone.self, forKey: .tone), title: try c.decode(String.self, forKey: .title), text: try c.decode(String.self, forKey: .text))
        case "flow": self = .flow(title: try c.decode(String.self, forKey: .title), steps: try c.decode([FlowStep].self, forKey: .steps))
        case "table": self = .table(title: try c.decode(String.self, forKey: .title), columns: try c.decode([String].self, forKey: .columns), rows: try c.decode([[String]].self, forKey: .rows))
        case "list": self = .list(title: try c.decode(String.self, forKey: .title), items: try c.decode([String].self, forKey: .items))
        default: throw DecodingError.dataCorruptedError(forKey: .type, in: c, debugDescription: "Unknown learn block type")
        }
    }
    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: Keys.self)
        switch self {
        case .paragraph(let text): try c.encode("paragraph", forKey: .type); try c.encode(text, forKey: .text)
        case .code(let language, let code, let caption): try c.encode("code", forKey: .type); try c.encode(language, forKey: .language); try c.encode(code, forKey: .code); try c.encode(caption, forKey: .caption)
        case .callout(let tone, let title, let text): try c.encode("callout", forKey: .type); try c.encode(tone, forKey: .tone); try c.encode(title, forKey: .title); try c.encode(text, forKey: .text)
        case .flow(let title, let steps): try c.encode("flow", forKey: .type); try c.encode(title, forKey: .title); try c.encode(steps, forKey: .steps)
        case .table(let title, let columns, let rows): try c.encode("table", forKey: .type); try c.encode(title, forKey: .title); try c.encode(columns, forKey: .columns); try c.encode(rows, forKey: .rows)
        case .list(let title, let items): try c.encode("list", forKey: .type); try c.encode(title, forKey: .title); try c.encode(items, forKey: .items)
        }
    }
}
struct FlowStep: Codable, Identifiable, Sendable { let id, label, detail: String }

struct Quiz: Codable, Identifiable, Sendable {
    let id, title, categoryId: String; let linkedLessonIds: [String]; let kind: QuizKind
    let difficulty: String; let passingScore: Double; let questions: [Question]; let rewards: Rewards; let version: String
}
struct Question: Codable, Identifiable, Sendable {
    let id, prompt: String; let type: QuestionType; let difficulty: String
    let options: [JSONValue]?; let answer: JSONValue; let acceptedAnswers: [String]?
    let explanation, lessonId: String
}
struct Rewards: Codable, Sendable { let xp, stars: Int }

struct Challenge: Codable, Identifiable, Sendable {
    let id, title, categoryId, lessonId, difficulty: String; let estimatedMinutes: Int
    let prompt: String; let successCriteria, hints: [String]; let starterCode: StarterCode
    let solutionOutline, verification: [String]; let rewards: Rewards; let version: String
}
struct StarterCode: Codable, Sendable { let language, code: String }
struct Badge: Codable, Identifiable, Sendable {
    let id, title, description, categoryId, icon: String; let criteria: BadgeCriteria; let xp: Int; let rarity, version: String
}
struct BadgeCriteria: Codable, Sendable { let type, targetId: String; let minimumScore: Double }
struct Glossary: Codable, Sendable { let id, title, version: String; let entries: [GlossaryEntry] }
struct GlossaryEntry: Codable, Identifiable, Sendable {
    let id, term, definition, categoryId: String; let tags, relatedLessonIds: [String]
}

struct RoadmapGraph: Codable, Sendable {
    let id: String; let curriculumVersion: String?; let nodes: [RoadmapNode]
    let edges: [RoadmapEdge]?; let adjacency, incoming: [String: [String]]?; let topologicalOrder: [String]?; let categories: [RoadmapCategory]?
}
struct RoadmapNode: Codable, Identifiable, Sendable {
    let id, categoryId, title: String; let type: RoadmapNodeType; let status: NodeStatus
    let lessonId, quizId: String?; let difficulty: String; let estimatedLearningMinutes: Int
    let unlockPrerequisites: [String]; let rewards: Rewards
}
struct RoadmapEdge: Codable, Sendable { let from, to, kind: String }
struct RoadmapCategory: Codable, Identifiable, Sendable {
    let id, title: String; let order: Int; let startNodeIds, checkpointNodeIds, previewNodeIds: [String]
}
struct SearchIndex: Codable, Sendable { let id, curriculumVersion: String; let normalization: String?; let documents: [SearchDocument] }
struct SearchDocument: Codable, Identifiable, Sendable {
    let id, type, title: String; let categoryId, lessonId: String?; let tags: [String]; let text: String
}

struct ContentIndex: Codable, Sendable {
    let id, curriculumVersion: String; let contentRevision: Int; let counts: ContentCounts
    let categories, lessons, quizzes, challenges, badges, roadmap: [IndexRecord]
}
struct ContentCounts: Codable, Sendable { let categories, lessons, quizzes, challenges, badges, glossaryEntries: Int }
struct IndexRecord: Codable, Sendable { let id, path, sha256: String }

struct RecallItem: Identifiable, Sendable { let id: String; let lesson: Lesson; let recall: Recall }

struct LoadedContent: Sendable {
    let curriculum: Curriculum; let index: ContentIndex; let categories: [Category]
    let lessonsById: [String: Lesson]; let quizzesById: [String: Quiz]
    let challengesById: [String: Challenge]; let badgesById: [String: Badge]
    let glossary: [GlossaryEntry]; let roadmap: RoadmapGraph; let search: SearchIndex

    var categoryById: [String: Category] { Dictionary(uniqueKeysWithValues: categories.map { ($0.id, $0) }) }
    var roadmapNodesById: [String: RoadmapNode] { Dictionary(uniqueKeysWithValues: roadmap.nodes.map { ($0.id, $0) }) }
    var recallItemsById: [String: RecallItem] {
        Dictionary(uniqueKeysWithValues: lessonsById.values.flatMap { lesson in
            lesson.revealStages.recall.enumerated().map { index, recall in
                let stableId = recall.id.isEmpty ? "\(lesson.id)-recall-\(index + 1)" : recall.id
                return (stableId, RecallItem(id: stableId, lesson: lesson, recall: recall))
            }
        })
    }
    func category(_ id: String?) -> Category? { id.flatMap { categoryById[$0] } }
    func lesson(_ id: String?) -> Lesson? { id.flatMap { lessonsById[$0] } }
    func quiz(_ id: String?) -> Quiz? { id.flatMap { quizzesById[$0] } }
    func challenge(_ id: String?) -> Challenge? { id.flatMap { challengesById[$0] } }
    func challenge(forLesson id: String?) -> Challenge? { challengesById.values.first { $0.lessonId == id } }
    func recallItem(_ id: String?) -> RecallItem? { id.flatMap { recallItemsById[$0] } }
}
