import SwiftUI

struct RootView: View {
    @EnvironmentObject private var model: AppModel
    @EnvironmentObject private var progress: ProgressStore

    var body: some View {
        ZStack {
            DQ.screen.ignoresSafeArea()
            switch model.loadState {
            case .loading: ProgressView("Loading curriculum…").tint(DQ.green).foregroundStyle(DQ.text.opacity(0.6))
            case .failure(let error): ErrorView(error: error) { model.loadContent() }
            case .success(let content): app(content)
            }
        }.tint(DQ.green)
    }

    @ViewBuilder private func app(_ content: LoadedContent) -> some View {
        VStack(spacing: 0) {
            ZStack(alignment: .bottomTrailing) {
                Group {
                    switch model.nav.screen {
                    case .home: HomeView(content: content)
                    case .map: QuestMapView(content: content)
                    case .region: RegionView(content: content)
                    case .topic: TopicView(content: content)
                    case .lesson: LessonView(content: content)
                    case .revision: QuizView(content: content)
                    case .review: ReviewView(content: content)
                    case .challenge: ChallengeView(content: content)
                    case .search: SearchView(content: content)
                    case .starred: StarredView(content: content)
                    case .settings: SettingsView(content: content)
                    }
                }.frame(maxWidth: .infinity, maxHeight: .infinity)
                if ![AppScreen.revision, .review, .settings].contains(model.nav.screen) { AIHelper().padding(.trailing, 20).padding(.bottom, 22) }
            }
            if model.nav.screen.isTopLevel { BottomNav(active: model.nav.screen) }
        }
    }
}

private struct BottomNav: View {
    @EnvironmentObject var model: AppModel; let active: AppScreen
    let tabs: [(AppScreen, String, String)] = [(.home,"house.fill","Home"),(.map,"diamond.fill","Map"),(.search,"magnifyingglass","Search"),(.starred,"star.fill","Starred"),(.settings,"gearshape.fill","Settings")]
    var body: some View {
        HStack { ForEach(tabs, id: \.0) { screen, icon, label in Button { model.go(to: screen) } label: { VStack(spacing: 3) { Image(systemName: icon).font(.system(size: 17)); Text(label).font(.system(size: 9.5, weight: .bold)) }.foregroundStyle(active == screen ? DQ.green : DQ.text.opacity(0.4)).frame(maxWidth: .infinity).padding(.vertical, 7) }.buttonStyle(.plain) } }
            .padding(.horizontal, 4).background(DQ.screen).overlay(alignment: .top) { Rectangle().fill(DQ.border).frame(height: 1) }
    }
}

private struct AIHelper: View {
    @EnvironmentObject var model: AppModel
    var body: some View { VStack(alignment: .trailing, spacing: 10) {
        if model.nav.aiOpen { VStack(alignment: .leading, spacing: 8) { Text("AI HELPER").font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.5)); Text("Optional hints live here. AI assistance is a network extra — all learning works fully offline without it.").font(.system(size: 13)).foregroundStyle(DQ.text) }.padding(14).frame(width: 230).dqCard(corner: 16, fill: DQ.cardAlt).transition(.scale) }
        Button { withAnimation { model.nav.aiOpen.toggle() } } label: { Text("✦").foregroundStyle(DQ.blueLight).frame(width: 46, height: 46).background(DQ.cardAlt).clipShape(Circle()).overlay(Circle().stroke(Color.white.opacity(0.12))) }.buttonStyle(.plain)
    } }
}

private struct ErrorView: View {
    let error: ContentFailure; let retry: () -> Void
    var title: String { switch error.kind { case .missingContent: "Content files are missing"; case .malformedJSON: "Content is malformed"; case .unsupportedVersion: "Content version unsupported"; case .hashMismatch: "Content integrity check failed"; case .unknown: "Content failed to load" } }
    var body: some View { VStack(spacing: 12) { Text("⚠").font(.system(size: 40)).foregroundStyle(DQ.amber); Text(title).font(.headline.bold()).foregroundStyle(DQ.text); Text(error.message).font(.caption).foregroundStyle(DQ.text.opacity(0.55)).multilineTextAlignment(.center); DQButton(title: "Retry", action: retry).frame(maxWidth: 220).padding(.top, 8) }.padding(32) }
}
