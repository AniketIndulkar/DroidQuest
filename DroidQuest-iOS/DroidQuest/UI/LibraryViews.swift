import SwiftUI

struct SearchView: View {
    @EnvironmentObject var model: AppModel; let content: LoadedContent
    private var results: [SearchDocument] {
        let query = model.nav.query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased(), filter = model.nav.tagFilter
        let titles = Dictionary(uniqueKeysWithValues: content.categories.map { ($0.id, $0.title) })
        return content.search.documents.filter { doc in
            (filter == "All" || doc.categoryId.flatMap { titles[$0] } == filter) && (query.isEmpty || doc.title.lowercased().contains(query) || doc.text.lowercased().contains(query) || doc.type.lowercased().contains(query) || doc.tags.contains { $0.lowercased().contains(query) })
        }
    }
    var body: some View { ScrollView { LazyVStack(alignment: .leading, spacing: 0) {
        Text("Search").font(.system(size: 22, weight: .black)).padding(.bottom, 14)
        TextField("Search lessons, quizzes, challenges, glossary…", text: $model.nav.query).textFieldStyle(.plain).padding(.horizontal, 18).padding(.vertical, 13).dqCard(corner: 30).padding(.bottom, 14)
        ScrollView(.horizontal, showsIndicators: false) { HStack { ForEach(["All"] + content.categories.map(\.title), id: \.self) { title in Button(title) { model.nav.tagFilter = title }.font(.caption.bold()).foregroundStyle(model.nav.tagFilter == title ? DQ.ink : DQ.text.opacity(0.65)).padding(.horizontal, 14).padding(.vertical, 8).background(model.nav.tagFilter == title ? DQ.green : DQ.text.opacity(0.08)).clipShape(Capsule()).buttonStyle(.plain) } } }.padding(.bottom, 16)
        Text("\(results.count) result\(results.count == 1 ? "" : "s")").font(.caption).foregroundStyle(DQ.text.opacity(0.4)).padding(.bottom, 10)
        if results.isEmpty { Text("No results. Try another term or filter.").font(.subheadline).foregroundStyle(DQ.text.opacity(0.4)).frame(maxWidth: .infinity).padding(.vertical, 40) }
        else { ForEach(Array(results.prefix(60))) { doc in SearchRow(doc: doc, content: content).padding(.bottom, 8) } }
    }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 84) }.foregroundStyle(DQ.text) }
}

private struct SearchRow: View {
    @EnvironmentObject var model: AppModel; let doc: SearchDocument; let content: LoadedContent
    var body: some View { let category = content.category(doc.categoryId), locked = category?.status == .planned, color = category?.accent ?? DQ.green
        Button { model.routeSearch(doc) } label: { HStack(spacing: 12) { Text(glyph).foregroundStyle(color).frame(width: 38, height: 38).background(color.opacity(0.16)).clipShape(RoundedRectangle(cornerRadius: 10)); VStack(alignment: .leading, spacing: 2) { Text(doc.title).font(.subheadline.bold()).lineLimit(1); Text("\(doc.type.capitalized) · \(category?.title ?? "")").font(.caption).foregroundStyle(DQ.text.opacity(0.45)).lineLimit(1) }; Spacer(); if locked { Text("Locked").font(.caption2.bold()).foregroundStyle(DQ.text.opacity(0.4)).padding(.horizontal, 8).padding(.vertical, 4).background(DQ.text.opacity(0.08)).clipShape(Capsule()) } }.padding(12).dqCard(corner: 14).opacity(locked ? 0.5 : 1) }.buttonStyle(.plain).disabled(locked)
    }
    private var glyph: String { ["lesson":"▸","quiz":"★","challenge":"⚑","glossary":"§","category":"◆"][doc.type] ?? "•" }
}

struct StarredView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let content: LoadedContent
    var lessons: [Lesson] { store.progress.starredLessonIds.compactMap { content.lesson($0) } }
    var body: some View { ScrollView { LazyVStack(alignment: .leading, spacing: 0) { Text("Starred Lessons").font(.system(size: 22, weight: .black)); Text("\(lessons.count) lesson\(lessons.count == 1 ? "" : "s") pinned").font(.subheadline).foregroundStyle(DQ.text.opacity(0.5)).padding(.bottom, 18)
        if lessons.isEmpty { Text("Star a lesson from its overview to pin it here for quick review.").font(.subheadline).foregroundStyle(DQ.text.opacity(0.4)).frame(maxWidth: .infinity).padding(.vertical, 60) }
        else { ForEach(content.categories) { category in let group = lessons.filter { $0.categoryId == category.id }; if !group.isEmpty { SectionLabel(text: category.title, color: category.accent).padding(.bottom, 8); ForEach(group) { lesson in Button { let node = content.roadmap.nodes.first { $0.lessonId == lesson.id }; model.openTopic(lesson.id, nodeId: node?.id, categoryId: lesson.categoryId) } label: { HStack { VStack(alignment: .leading) { Text(lesson.title).font(.subheadline.bold()); Text("Week \(lesson.week) · ~\(lesson.estimatedLearningMinutes) min").font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Spacer(); Button("★") { store.toggleStar(lesson.id) }.foregroundStyle(DQ.amber).font(.title3) }.padding(13).dqCard(corner: 14) }.buttonStyle(.plain).padding(.bottom, 8) }; Spacer().frame(height: 10) } } }
    }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 84) }.foregroundStyle(DQ.text) }
}

struct SettingsView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let content: LoadedContent
    var body: some View { let progress = store.progress, settings = progress.settings
        ScrollView { VStack(alignment: .leading, spacing: 0) { Text("Settings").font(.system(size: 22, weight: .black)).padding(.bottom, 18)
            HStack(spacing: 14) { Text("You").font(.caption.bold()).foregroundStyle(DQ.blueLight).frame(width: 52, height: 52).background(DQ.blue.opacity(0.18)).clipShape(Circle()); VStack(alignment: .leading) { Text("Learner").font(.headline.bold()); Text("Level \(UIDerive.currentLevel(content, progress)) · \(progress.totalXp) XP · \(progress.totalStars)★").font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Spacer() }.padding(16).dqCard().padding(.bottom, 20)
            SectionLabel(text: "Sync").padding(.bottom, 8); VStack(spacing: 0) { HStack { Text("GH").font(.caption.bold()).frame(width: 34, height: 34).background(DQ.text.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 9)); VStack(alignment: .leading) { Text("GitHub Sync").font(.subheadline.bold()); Text(settings.githubConnected ? "Connected · optional" : "Not connected · optional").font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Spacer(); Toggle("", isOn: Binding(get: { settings.githubConnected }, set: { store.setGithub($0) })).labelsHidden() }.padding(14); divider; Button("Back up progress now") { store.setGithub(true) }.font(.subheadline.bold()).frame(maxWidth: .infinity, alignment: .leading).padding(14); Text("Local-first: progress lives on this device and syncs to GitHub only when connected.").font(.caption2).foregroundStyle(DQ.text.opacity(0.35)).padding(.horizontal, 14).padding(.bottom, 12) }.dqCard().padding(.bottom, 20)
            SectionLabel(text: "Learning").padding(.bottom, 8); VStack(spacing: 0) { toggle("Daily reminders", settings.notifications) { store.setNotifications($0) }; divider; toggle("Sound effects", settings.sound) { store.setSound($0) } }.dqCard().padding(.bottom, 20)
            SectionLabel(text: "About").padding(.bottom, 8); VStack(spacing: 0) { info("Curriculum version", content.curriculum.version); divider; info("Levels", "\(content.categories.count) (2 available)"); divider; info("Lessons", "\(content.lessonsById.count)") }.dqCard().padding(.bottom, 20)
            Text("DroidQuest · \(content.curriculum.title)").font(.caption).foregroundStyle(DQ.text.opacity(0.3)).frame(maxWidth: .infinity)
        }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 84) }.foregroundStyle(DQ.text)
    }
    private var divider: some View { Rectangle().fill(DQ.border).frame(height: 1).padding(.horizontal, 14) }
    private func toggle(_ title: String, _ value: Bool, action: @escaping (Bool) -> Void) -> some View { HStack { Text(title).font(.subheadline); Spacer(); Toggle("", isOn: Binding(get: { value }, set: action)).labelsHidden() }.padding(14) }
    private func info(_ title: String, _ value: String) -> some View { HStack { Text(title).font(.subheadline); Spacer(); Text(value).font(.caption).foregroundStyle(DQ.text.opacity(0.5)) }.padding(14) }
}
