import SwiftUI

struct HomeView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore
    let content: LoadedContent
    var body: some View {
        let progress = store.progress, level = UIDerive.currentLevel(content, progress), next = UIDerive.nextNode(content, progress)
        let current = next.flatMap { content.category($0.categoryId) } ?? content.categories[0]
        ScrollView { LazyVStack(spacing: 18) {
            HStack { ZStack { Circle().fill(DQ.card).overlay(Circle().stroke(DQ.green, lineWidth: 2)); Text("◆").foregroundStyle(DQ.green).font(.caption) }.frame(width: 34, height: 34); Text("DroidQuest").font(.system(size: 18, weight: .bold)); Spacer(); pill("\(UIDerive.totalStars(content, progress))★", DQ.amber); pill("Lv \(level)", DQ.blueLight) }
            if progress.reviewsDue() > 0 { Button { model.startDailyReview() } label: { HStack { VStack(alignment: .leading, spacing: 4) { Text("REVIEW DUE").font(.caption.bold()).foregroundStyle(DQ.blueLight); Text("Strengthen \(progress.reviewsDue()) memories").font(.headline.bold()); Text("A short, mixed session—no penalties.").font(.caption).foregroundStyle(DQ.text.opacity(0.5)) }; Spacer(); Text("Review ›").font(.caption.bold()).foregroundStyle(DQ.blueLight) }.padding(18).dqCard(corner: 20, fill: DQ.blue.opacity(0.1)) }.buttonStyle(.plain) }
            HStack(spacing: 16) { ZStack { Circle().stroke(DQ.text.opacity(0.1), lineWidth: 7); Circle().trim(from: 0, to: CGFloat(UIDerive.categoryProgress(content, progress, current.id).percent) / 100).stroke(DQ.green, style: StrokeStyle(lineWidth: 7, lineCap: .round)).rotationEffect(.degrees(-90)); VStack(spacing: 0) { Text("Lv\(level)").font(.headline.bold()); Text("\(UIDerive.categoryProgress(content, progress, current.id).percent)%").font(.caption2).foregroundStyle(DQ.text.opacity(0.5)) } }.frame(width: 74, height: 74); VStack(alignment: .leading, spacing: 4) { Text("\(progress.totalXp) XP").font(.caption).foregroundStyle(DQ.text.opacity(0.6)); Text(current.title).font(.headline.bold()); Text("Local-first progress · streak not tracked yet").font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Spacer() }.padding(18).dqCard(corner: 20)
            if let next { Button { model.openNode(next.id) } label: { VStack(alignment: .leading, spacing: 8) { HStack { Text("NEXT QUEST").font(.caption.bold()).foregroundStyle(DQ.amber); Spacer(); Text("+\(next.rewards.xp) XP · \(next.rewards.stars)★").font(.caption.bold()).foregroundStyle(DQ.amber) }; Text(next.title).font(.headline.bold()); Text("\(content.category(next.categoryId)?.title ?? "") · ~\(next.estimatedLearningMinutes) min").font(.caption).foregroundStyle(DQ.text.opacity(0.55)); Text("Continue ›").font(.subheadline.bold()).foregroundStyle(DQ.amber) }.frame(maxWidth: .infinity, alignment: .leading).padding(18).dqCard(corner: 20, fill: DQ.amber.opacity(0.1)) }.buttonStyle(.plain) }
            HStack { SectionLabel(text: "Quest regions"); Button("View map ›") { model.go(to: .map) }.font(.caption.bold()).foregroundStyle(DQ.green) }
            ForEach(content.categories) { category in HomeCategoryRow(category: category, content: content) }
        }.padding(.horizontal, 20).padding(.top, 20).padding(.bottom, 84) }.foregroundStyle(DQ.text)
    }
    private func pill(_ text: String, _ color: Color) -> some View { Text(text).font(.caption.bold()).foregroundStyle(color).padding(.horizontal, 10).padding(.vertical, 6).background(color.opacity(0.15)).clipShape(Capsule()) }
}

private struct HomeCategoryRow: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore
    let category: Category; let content: LoadedContent
    var body: some View {
        let cp = UIDerive.categoryProgress(content, store.progress, category.id), unlocked = ProgressionPolicy.isCategoryUnlocked(content.roadmap, categoryId: category.id, completed: store.progress.completedNodeIds), planned = category.status == .planned
        Button { model.openCategory(category.id) } label: { HStack(spacing: 14) { ZStack { RoundedRectangle(cornerRadius: 14).fill(category.accent.opacity(0.16)); Text(iconGlyph(category.theme.icon)).font(.title3.bold()).foregroundStyle(category.accent) }.frame(width: 52, height: 52); VStack(alignment: .leading, spacing: 5) { HStack { Text(category.title).font(.subheadline.bold()).lineLimit(1); Spacer(); Text("\(cp.starsEarned)★").font(.caption.bold()).foregroundStyle(category.accent) }; Text(planned ? "Planned preview" : (!unlocked ? "Locked" : "\(cp.completed)/\(cp.total) nodes · \(cp.percent)%")).font(.caption).foregroundStyle(DQ.text.opacity(0.45)); if !planned { ProgressBar(percent: cp.percent, color: category.accent) } } }.padding(13).dqCard(corner: 16).opacity((planned || !unlocked) ? 0.58 : 1) }.buttonStyle(.plain)
    }
}

struct QuestMapView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let content: LoadedContent
    var body: some View { ScrollView { LazyVStack(alignment: .leading, spacing: 0) {
        Text("Quest Map").font(.system(size: 24, weight: .black)); Text("Beginner to Android platform expert").font(.subheadline).foregroundStyle(DQ.text.opacity(0.5)).padding(.bottom, 18)
        HStack { stat("\(store.progress.completedNodeIds.count)", "/ \(content.roadmap.nodes.filter{$0.type != .levelPreview}.count)", "Nodes complete", DQ.text); Spacer(); Divider().frame(height: 32).overlay(DQ.border); Spacer(); stat("\(UIDerive.totalStars(content, store.progress))", "/ \(UIDerive.maxStars(content))", "Stars earned", DQ.amber) }.padding(16).dqCard().padding(.bottom, 22)
        ForEach(Array(content.categories.enumerated()), id: \.element.id) { index, category in if index > 0 { Rectangle().fill(Color.white.opacity(0.12)).frame(width: 3, height: 26).padding(.leading, 31) }; MapCategoryRow(category: category, content: content) }
    }.padding(.horizontal, 20).padding(.top, 20).padding(.bottom, 84) }.foregroundStyle(DQ.text) }
    private func stat(_ main: String, _ sub: String, _ label: String, _ color: Color) -> some View { VStack(alignment: .leading) { HStack(alignment: .lastTextBaseline, spacing: 2) { Text(main).font(.system(size: 20, weight: .black)).foregroundStyle(color); Text(sub).font(.caption).foregroundStyle(DQ.text.opacity(0.4)) }; Text(label).font(.caption2).foregroundStyle(DQ.text.opacity(0.45)) } }
}

private struct MapCategoryRow: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let category: Category; let content: LoadedContent
    var body: some View {
        let cp = UIDerive.categoryProgress(content, store.progress, category.id), planned = category.status == .planned, unlocked = ProgressionPolicy.isCategoryUnlocked(content.roadmap, categoryId: category.id, completed: store.progress.completedNodeIds), complete = cp.total > 0 && cp.completed == cp.total, locked = planned || !unlocked
        Button { model.openCategory(category.id) } label: { HStack(spacing: 14) { ZStack { Circle().fill(complete ? category.accent : (locked ? DQ.badgeDim : DQ.card)).overlay(Circle().stroke(locked ? Color.white.opacity(0.15) : category.accent, lineWidth: 2)); Text(complete ? "✓" : (locked ? "■" : "◆")).font(.headline.bold()).foregroundStyle(complete ? DQ.screen : (locked ? DQ.text.opacity(0.35) : category.accent)) }.frame(width: 64, height: 64); VStack(alignment: .leading, spacing: 3) { HStack { Text(category.title).font(.subheadline.bold()).lineLimit(1); Spacer(); Text("\(cp.starsEarned)★").font(.caption.bold()).foregroundStyle(category.accent) }; Text(planned ? "Planned preview · unlocks in order" : (!unlocked ? "Locked · complete the previous level" : (complete ? "Completed · \(cp.total) nodes" : "\(cp.completed)/\(cp.total) nodes · in progress"))).font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }.padding(13).dqCard(corner: 14) }.opacity(locked ? 0.55 : 1) }.buttonStyle(.plain)
    }
}

struct RegionView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let content: LoadedContent
    var body: some View { if let category = content.category(model.nav.categoryId) { let cp = UIDerive.categoryProgress(content, store.progress, category.id); let nodes = UIDerive.categoryNodes(content, category.id)
        ScrollView { LazyVStack(alignment: .leading, spacing: 0) {
            VStack(alignment: .leading, spacing: 8) { HStack { Button("←") { model.back() }.font(.title3); Text("LEVEL \(category.order)").font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.5)) }; Text(category.title).font(.system(size: 24, weight: .black)); Text(category.description).font(.subheadline).foregroundStyle(DQ.text.opacity(0.6)); if category.status != .planned { ProgressBar(percent: cp.percent, color: category.accent); HStack { Text("\(cp.completed) / \(cp.total) nodes"); Spacer(); Text("\(cp.starsEarned)★").foregroundStyle(DQ.amber) }.font(.caption) } else { Text("Planned preview · Weeks \(category.weekRange.start)–\(category.weekRange.end)").font(.caption.bold()).foregroundStyle(category.accent) } }.padding(20).background(category.accent.opacity(0.14))
            VStack(alignment: .leading, spacing: 18) { VStack(alignment: .leading, spacing: 6) { Text("PROJECT · \(category.project.title)".uppercased()).font(.caption.bold()).foregroundStyle(category.accent); Text(category.project.summary).font(.system(size: 13)).foregroundStyle(DQ.text.opacity(0.7)) }.padding(14).dqCard(corner: 14)
                if category.status == .planned || nodes.isEmpty { SectionLabel(text: "Planned topics"); ForEach(category.plannedTopics, id: \.self) { Text("○  \($0)").font(.subheadline).foregroundStyle(DQ.text.opacity(0.7)).padding(12).frame(maxWidth: .infinity, alignment: .leading).dqCard(corner: 12) } }
                else { ForEach(nodes) { NodeRow(node: $0, category: category) } }
            }.padding(20)
        }.padding(.bottom, 60) }.foregroundStyle(DQ.text)
    } }
}

private struct NodeRow: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let node: RoadmapNode; let category: Category
    var body: some View { let progress = ProgressionPolicy.progress(of: node, completed: store.progress.completedNodeIds), enabled = progress != .locked
        Button { model.openNode(node.id) } label: { HStack(spacing: 12) { ZStack { (node.type == .boss || node.type == .checkpoint ? AnyShape(RoundedRectangle(cornerRadius: 14)) : AnyShape(Circle())).fill(progress == .completed ? category.accent : (progress == .locked ? DQ.badgeDim : DQ.card)).overlay((node.type == .boss || node.type == .checkpoint ? AnyShape(RoundedRectangle(cornerRadius: 14)) : AnyShape(Circle())).stroke(progress == .locked ? Color.white.opacity(0.15) : category.accent, lineWidth: 2)); Text(progress == .completed ? "✓" : (progress == .locked ? "■" : ((node.type == .boss || node.type == .checkpoint) ? "★" : "▸"))).font(.headline.bold()).foregroundStyle(progress == .completed ? DQ.screen : category.accent) }.frame(width: 46, height: 46); VStack(alignment: .leading, spacing: 3) { Text(node.title).font(.subheadline.bold()); Text(node.type == .boss ? "Boss · Level checkpoint" : (node.type == .checkpoint ? "Weekly checkpoint" : "Lesson · \(node.estimatedLearningMinutes) min")).font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }.frame(maxWidth: .infinity, alignment: .leading).padding(13).dqCard(corner: 14); if progress == .completed { Text("\(node.rewards.stars)★").font(.caption.bold()).foregroundStyle(category.accent) } }.opacity(enabled ? 1 : 0.5) }.buttonStyle(.plain).disabled(!enabled)
    }
}
