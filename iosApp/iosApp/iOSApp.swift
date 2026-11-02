import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif

        guard let baseUrl = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String,
              !baseUrl.isEmpty else {
            fatalError("API_BASE_URL missing from Info.plist / Config.xcconfig")
        }
        IosEntryKt.startKoinIos(baseUrl: baseUrl, isDebug: isDebug)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
