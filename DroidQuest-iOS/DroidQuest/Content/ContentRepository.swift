import CryptoKit
import Foundation

enum ContentErrorKind: String, Sendable { case missingContent, malformedJSON, unsupportedVersion, hashMismatch, unknown }
struct ContentFailure: Error, LocalizedError, Sendable {
    let kind: ContentErrorKind; let message: String
    var errorDescription: String? { message }
}

struct ContentSource: Sendable {
    let root: URL
    func read(_ path: String) throws -> Data {
        let url = root.appendingPathComponent(path)
        guard FileManager.default.fileExists(atPath: url.path) else {
            throw ContentFailure(kind: .missingContent, message: "Missing content file: \(path)")
        }
        return try Data(contentsOf: url, options: .mappedIfSafe)
    }
    static func bundled(_ bundle: Bundle = .main) throws -> ContentSource {
        guard let root = bundle.resourceURL else {
            throw ContentFailure(kind: .missingContent, message: "The application resource bundle is unavailable.")
        }
        return ContentSource(root: root)
    }
}

struct ContentRepository: Sendable {
    static let appContentAPI = 2
    let source: ContentSource
    private let decoder = JSONDecoder()

    func load() throws -> LoadedContent {
        let index: ContentIndex = try parse("content/generated/content-index.json")
        let curriculum: Curriculum = try parse("content/curriculum.json")
        guard curriculum.minimumAppContentApi <= Self.appContentAPI else {
            throw ContentFailure(kind: .unsupportedVersion, message: "Content requires app content API \(curriculum.minimumAppContentApi), but this build supports \(Self.appContentAPI). Update the app.")
        }
        guard index.curriculumVersion == curriculum.version else {
            throw ContentFailure(kind: .unsupportedVersion, message: "Content index version \(index.curriculumVersion) does not match curriculum \(curriculum.version).")
        }

        let categories: [Category] = try index.categories.map(parseVerified)
        let lessons: [Lesson] = try index.lessons.map(parseVerified)
        let quizzes: [Quiz] = try index.quizzes.map(parseVerified)
        let challenges: [Challenge] = try index.challenges.map(parseVerified)
        let badges: [Badge] = try index.badges.map(parseVerified)
        for record in index.roadmap { let _: JSONValue = try parseVerified(record) }

        let roadmap: RoadmapGraph = try parse("content/generated/roadmap-graph.json")
        let search: SearchIndex = try parse("content/generated/search-index.json")
        var glossary: [GlossaryEntry] = []
        for id in curriculum.glossaryIds {
            let file: Glossary = try parse("content/glossary/\(id).json")
            glossary.append(contentsOf: file.entries)
        }
        try verifyCounts(index.counts, categories.count, lessons.count, quizzes.count, challenges.count, badges.count, glossary.count)
        return LoadedContent(
            curriculum: curriculum, index: index, categories: categories.sorted { $0.order < $1.order },
            lessonsById: Dictionary(uniqueKeysWithValues: lessons.map { ($0.id, $0) }),
            quizzesById: Dictionary(uniqueKeysWithValues: quizzes.map { ($0.id, $0) }),
            challengesById: Dictionary(uniqueKeysWithValues: challenges.map { ($0.id, $0) }),
            badgesById: Dictionary(uniqueKeysWithValues: badges.map { ($0.id, $0) }),
            glossary: glossary, roadmap: roadmap, search: search
        )
    }

    private func parse<T: Decodable>(_ path: String) throws -> T {
        do { return try decoder.decode(T.self, from: source.read(path)) }
        catch let error as ContentFailure { throw error }
        catch { throw ContentFailure(kind: .malformedJSON, message: "Malformed JSON in \(path): \(error.localizedDescription)") }
    }

    private func parseVerified<T: Decodable>(_ record: IndexRecord) throws -> T {
        let data = try source.read(record.path)
        let actual = SHA256.hash(data: data).map { String(format: "%02x", $0) }.joined()
        guard actual.caseInsensitiveCompare(record.sha256) == .orderedSame else {
            throw ContentFailure(kind: .hashMismatch, message: "Hash mismatch for \(record.id) (\(record.path)): expected \(record.sha256), got \(actual)")
        }
        do { return try decoder.decode(T.self, from: data) }
        catch { throw ContentFailure(kind: .malformedJSON, message: "Malformed JSON in \(record.path): \(error.localizedDescription)") }
    }

    private func verifyCounts(_ expected: ContentCounts, _ categories: Int, _ lessons: Int, _ quizzes: Int, _ challenges: Int, _ badges: Int, _ glossary: Int) throws {
        let pairs = [
            ("categories", expected.categories, categories), ("lessons", expected.lessons, lessons),
            ("quizzes", expected.quizzes, quizzes), ("challenges", expected.challenges, challenges),
            ("badges", expected.badges, badges), ("glossary", expected.glossaryEntries, glossary),
        ]
        let mismatches = pairs.filter { $0.1 != $0.2 }.map { "\($0.0) \($0.1)!=\($0.2)" }
        guard mismatches.isEmpty else {
            throw ContentFailure(kind: .missingContent, message: "Content index counts do not match loaded records: \(mismatches.joined(separator: ", "))")
        }
    }
}
