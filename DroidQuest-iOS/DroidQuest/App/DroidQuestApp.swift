import SwiftUI

@main
struct DroidQuestApp: App {
    @StateObject private var model = AppModel()
    var body: some Scene { WindowGroup { RootView().environmentObject(model).environmentObject(model.progressStore).preferredColorScheme(.dark) } }
}

