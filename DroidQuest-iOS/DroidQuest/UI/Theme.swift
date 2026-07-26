import SwiftUI

enum DQ {
    static let screen = Color(hex: "14181A"), card = Color(hex: "1E2422"), cardAlt = Color(hex: "262E2B"), badgeDim = Color(hex: "2A322F")
    static let green = Color(hex: "3DDC84"), amber = Color(hex: "F2B33D"), blue = Color(hex: "4C8DFF"), blueLight = Color(hex: "7FADFF")
    static let orange = Color(hex: "E2663C"), red = Color(hex: "E2574C"), ink = Color(hex: "0B0D0C"), text = Color(hex: "F4F2EE")
    static let border = Color.white.opacity(0.06), starOff = text.opacity(0.2)
}

extension Color {
    init(hex: String) {
        let value = UInt64(hex.trimmingCharacters(in: CharacterSet(charactersIn: "#")), radix: 16) ?? 0x3DDC84
        self.init(.sRGB, red: Double((value >> 16) & 255) / 255, green: Double((value >> 8) & 255) / 255, blue: Double(value & 255) / 255)
    }
}

extension Category { var accent: Color { Color(hex: theme.color) } }

struct CardStyle: ViewModifier {
    var corner: CGFloat = 16; var fill: Color = DQ.card
    func body(content: Content) -> some View { content.background(fill).clipShape(RoundedRectangle(cornerRadius: corner)).overlay(RoundedRectangle(cornerRadius: corner).stroke(DQ.border)) }
}
extension View { func dqCard(corner: CGFloat = 16, fill: Color = DQ.card) -> some View { modifier(CardStyle(corner: corner, fill: fill)) } }

struct SectionLabel: View {
    let text: String; var color: Color = DQ.text.opacity(0.5)
    var body: some View { Text(text.uppercased()).font(.system(size: 12, weight: .bold)).tracking(0.5).foregroundStyle(color).frame(maxWidth: .infinity, alignment: .leading) }
}

struct DQButton: View {
    let title: String; var color: Color = DQ.green; var enabled = true; let action: () -> Void
    var body: some View {
        Button(action: action) { Text(title).font(.system(size: 15, weight: .heavy)).foregroundStyle(DQ.ink).frame(maxWidth: .infinity).padding(.vertical, 15).background(enabled ? color : DQ.text.opacity(0.15)).clipShape(RoundedRectangle(cornerRadius: 14)) }
            .buttonStyle(.plain).disabled(!enabled)
    }
}

struct ProgressBar: View {
    let percent: Int; let color: Color
    var body: some View { GeometryReader { geo in ZStack(alignment: .leading) { Capsule().fill(DQ.text.opacity(0.1)); Capsule().fill(color).frame(width: geo.size.width * CGFloat(max(0, min(100, percent))) / 100) } }.frame(height: 7) }
}

func iconGlyph(_ icon: String) -> String {
    ["terminal":"⌘", "android":"◈", "layers":"≣", "sync":"↻", "account-tree":"⑂", "devices":"▦", "verified":"✓", "security":"⛨", "build":"⚙", "speed":"◉", "memory":"▤", "route":"⌥"][icon] ?? "◆"
}

