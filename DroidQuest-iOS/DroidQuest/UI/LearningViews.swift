import SwiftUI
import UIKit

struct BackHeader: View {
    @EnvironmentObject var model: AppModel; let label: String
    var body: some View { HStack(spacing: 10) { Button("←") { model.back() }.font(.title3); Text(label.uppercased()).font(.caption.bold()).tracking(0.5).foregroundStyle(DQ.text.opacity(0.45)); Spacer() } }
}

struct TopicView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; @Environment(\.openURL) var openURL
    let content: LoadedContent
    var body: some View { if let lesson = content.lesson(model.nav.lessonId) { let category = content.category(lesson.categoryId), starred = store.progress.starredLessonIds.contains(lesson.id)
        ScrollView { VStack(alignment: .leading, spacing: 0) {
            HStack { Button("←") { model.back() }.font(.title3); Spacer(); Button(starred ? "★" : "☆") { store.toggleStar(lesson.id) }.font(.title3).foregroundStyle(starred ? DQ.amber : DQ.text.opacity(0.4)) }.padding(.bottom, 16)
            Text(category?.title ?? "").font(.caption).foregroundStyle(DQ.text.opacity(0.45)); Text(lesson.title).font(.system(size: 22, weight: .black)).padding(.vertical, 8)
            ScrollView(.horizontal, showsIndicators: false) { HStack { chip(lesson.difficulty.capitalized, category?.accent ?? DQ.green); chip("~\(lesson.estimatedLearningMinutes) min learn", DQ.text.opacity(0.65)); ForEach(lesson.tags, id: \.self) { chip($0, DQ.text.opacity(0.65)) } } }.padding(.bottom, 18)
            SectionLabel(text: "Why this matters").padding(.bottom, 8); Text(lesson.revealStages.scout.purpose).bodyText().padding(.bottom, 12); scout("Real Android use", lesson.revealStages.scout.realWorldUse); scout("You'll be able to", lesson.revealStages.scout.outcome)
            SectionLabel(text: "What you'll learn").padding(.top, 10).padding(.bottom, 8); ForEach(lesson.revision.objectives, id: \.self) { Text("✓  \($0)").bodyText().padding(.vertical, 3) }.padding(.bottom, 18)
            DQButton(title: "Begin Lesson") { model.openLesson() }
            if let challenge = content.challenge(forLesson: lesson.id) { SectionLabel(text: "Practice challenge").padding(.top, 22).padding(.bottom, 8); Button { model.openChallenge(challenge.id) } label: { VStack(alignment: .leading, spacing: 4) { Text(challenge.title).font(.subheadline.bold()); Text("~\(challenge.estimatedMinutes) min · +\(challenge.rewards.xp) XP · optional").font(.caption).foregroundStyle(DQ.text.opacity(0.5)) }.frame(maxWidth: .infinity, alignment: .leading).padding(14).dqCard(corner: 14) }.buttonStyle(.plain) }
            SectionLabel(text: "Further reading").padding(.top, 22).padding(.bottom, 8); ForEach(lesson.revealStages.learn.furtherReading) { reading in ReadingCard(reading: reading) { if let url = URL(string: reading.url) { openURL(url) } }.padding(.bottom, 8) }
        }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 70) }.foregroundStyle(DQ.text)
    } }
    private func chip(_ text: String, _ color: Color) -> some View { Text(text).font(.caption.bold()).foregroundStyle(color).padding(.horizontal, 10).padding(.vertical, 5).background(color.opacity(0.12)).clipShape(Capsule()) }
    private func scout(_ label: String, _ text: String) -> some View { VStack(alignment: .leading, spacing: 2) { Text(label).font(.caption.bold()).foregroundStyle(DQ.blueLight); Text(text).bodyText() }.padding(.bottom, 10) }
}

struct LessonView: View {
    @EnvironmentObject var model: AppModel; @Environment(\.openURL) var openURL; let content: LoadedContent
    var body: some View { if let lesson = content.lesson(model.nav.lessonId) { let stages = lesson.revealStages
        ScrollView { LazyVStack(alignment: .leading, spacing: 0) {
            BackHeader(label: "Lesson").padding(.bottom, 14); Text(lesson.title).font(.system(size: 21, weight: .black)).padding(.bottom, 18)
            Stage(title: "Scout") { VStack(alignment: .leading, spacing: 10) { scout("Why it matters", stages.scout.purpose); scout("Real Android use", stages.scout.realWorldUse); scout("Outcome", stages.scout.outcome) } }
            Stage(title: "Learn · ~\(stages.learn.estimatedMinutes) min") { ForEach(stages.learn.sections) { section in VStack(alignment: .leading, spacing: 12) { Text(section.title).font(.headline.bold()); ForEach(section.blocks) { LearnBlockView(block: $0) } }.padding(.bottom, 18) } }
            Stage(title: "Further reading") { ForEach(stages.learn.furtherReading) { reading in ReadingCard(reading: reading) { if let url = URL(string: reading.url) { openURL(url) } }.padding(.bottom, 8) } }
            Stage(title: "Inspect · \(stages.inspect.title)") { CodeView(language: stages.inspect.language, code: stages.inspect.code); Text("Walkthrough").font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.55)).padding(.top, 12); ForEach(Array(stages.inspect.walkthrough.enumerated()), id: \.offset) { i, step in HStack(alignment: .top) { Text("\(i + 1).").foregroundStyle(DQ.blue).bold(); Text(step).bodyText() }.padding(.vertical, 3) }; VStack(alignment: .leading, spacing: 4) { Text("EXPECTED OUTPUT").font(.caption.bold()).foregroundStyle(DQ.green); Text(stages.inspect.expectedOutput).font(.system(size: 13, design: .monospaced)) }.padding(12).frame(maxWidth: .infinity, alignment: .leading).dqCard(corner: 10, fill: DQ.green.opacity(0.08)).padding(.top, 10) }
            Stage(title: "Trap Check") { ForEach(stages.trapCheck) { trap in VStack(alignment: .leading, spacing: 5) { Text("✗ \(trap.mistake)").font(.subheadline.bold()).foregroundStyle(DQ.red); Text("Why: \(trap.why)").bodyText(); Text("Fix: \(trap.fix)").bodyText().foregroundStyle(DQ.green) }.padding(13).dqCard(corner: 12, fill: DQ.red.opacity(0.08)).padding(.bottom, 10) } }
            Stage(title: "Challenge") { Text(stages.challengeIntro.task).bodyText(); Text("Success looks like: \(stages.challengeIntro.successLooksLike)").bodyText().foregroundStyle(DQ.text.opacity(0.6)).padding(.top, 8); if let c = content.challenge(forLesson: lesson.id) { Button("Open practice challenge ›") { model.openChallenge(c.id) }.font(.caption.bold()).padding(.top, 10) } }
            Stage(title: "Recall") { Text("Answer from memory before looking. Your wording does not need to match exactly.").font(.caption).foregroundStyle(DQ.text.opacity(0.55)).padding(.bottom, 10); ForEach(Array(stages.recall.enumerated()), id: \.offset) { index, recall in RecallCard(lesson: lesson, recall: recall, stableId: recall.id.isEmpty ? "\(lesson.id)-recall-\(index + 1)" : recall.id).padding(.bottom, 10) } }
            DQButton(title: "Practice This Quest") { model.startQuiz(lesson.quizId) }
        }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 70) }.foregroundStyle(DQ.text)
    } }
    private func scout(_ title: String, _ value: String) -> some View { VStack(alignment: .leading, spacing: 2) { Text(title).font(.caption.bold()).foregroundStyle(DQ.blueLight); Text(value).bodyText() } }
}

private struct Stage<Content: View>: View {
    let title: String; @ViewBuilder let content: Content
    init(title: String, @ViewBuilder content: () -> Content) { self.title = title; self.content = content() }
    var body: some View { VStack(alignment: .leading, spacing: 12) { Text(title).font(.caption.bold()).tracking(0.5).foregroundStyle(DQ.green).padding(.horizontal, 10).padding(.vertical, 4).background(DQ.green.opacity(0.16)).clipShape(Capsule()); content }.frame(maxWidth: .infinity, alignment: .leading).padding(.bottom, 22) }
}

private struct RecallCard: View {
    @EnvironmentObject var model: AppModel; let lesson: Lesson; let recall: Recall; let stableId: String
    @State private var answer = ""; @State private var revealed = false; @State private var rating: ReviewRating?
    var body: some View { VStack(alignment: .leading, spacing: 10) { Text(recall.prompt).font(.subheadline.bold()); TextField("Write what you remember…", text: $answer, axis: .vertical).textFieldStyle(.plain).padding(11).background(DQ.screen).clipShape(RoundedRectangle(cornerRadius: 10)).disabled(revealed); if revealed { Text("MODEL ANSWER").font(.caption2.bold()).foregroundStyle(DQ.blueLight); Text(recall.answer).bodyText(); if let rating { Text("Scheduled for review · \(rating.rawValue)").font(.caption.bold()).foregroundStyle(DQ.green) } else { Text("How well did you remember it?").font(.caption).foregroundStyle(DQ.text.opacity(0.5)); HStack { ForEach(ReviewRating.allCases, id: \.self) { r in Button(r.rawValue.capitalized) { rating = r; model.rateRecall(lessonId: lesson.id, recallId: stableId, rating: r) }.font(.caption2.bold()).frame(maxWidth: .infinity).padding(.vertical, 9).background((r == .again ? DQ.amber : (r == .hard ? DQ.blueLight : DQ.green)).opacity(0.14)).clipShape(RoundedRectangle(cornerRadius: 9)) } } } } else { Button("Compare answer") { revealed = true }.font(.caption.bold()).foregroundStyle(answer.isEmpty ? DQ.text.opacity(0.28) : DQ.green).frame(maxWidth: .infinity).padding(10).background(DQ.green.opacity(answer.isEmpty ? 0.03 : 0.1)).clipShape(RoundedRectangle(cornerRadius: 9)).disabled(answer.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty) } }.padding(13).dqCard(corner: 12) }
}

struct LearnBlockView: View {
    let block: LearnBlock
    var body: some View { switch block {
    case .paragraph(let text): Text(text).bodyText()
    case .code(let language, let code, let caption): VStack(alignment: .leading, spacing: 6) { CodeView(language: language, code: code); if !caption.isEmpty { Text(caption).font(.caption).foregroundStyle(DQ.text.opacity(0.45)) } }
    case .callout(let tone, let title, let text): let color = tone == .note ? DQ.blue : (tone == .remember ? DQ.green : DQ.red); VStack(alignment: .leading, spacing: 6) { HStack { Text(tone.rawValue.uppercased()).font(.caption2.bold()).foregroundStyle(color); Text(title).font(.subheadline.bold()) }; Text(text).bodyText() }.padding(14).dqCard(corner: 12, fill: color.opacity(0.1))
    case .flow(let title, let steps): VStack(alignment: .leading, spacing: 8) { Text(title).font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.55)); ForEach(Array(steps.enumerated()), id: \.element.id) { i, step in HStack(alignment: .top, spacing: 12) { Text("\(i + 1)").font(.caption.bold()).foregroundStyle(DQ.blueLight).frame(width: 26, height: 26).background(DQ.blue.opacity(0.18)).clipShape(Circle()); VStack(alignment: .leading) { Text(step.label).font(.subheadline.bold()); Text(step.detail).bodyText() } } } }
    case .table(let title, let columns, let rows): VStack(alignment: .leading, spacing: 8) { Text(title).font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.55)); ScrollView(.horizontal) { VStack(alignment: .leading, spacing: 0) { HStack(spacing: 0) { ForEach(columns, id: \.self) { Text($0).font(.caption.bold()).frame(width: 150, alignment: .leading).padding(10) } }.background(Color.white.opacity(0.04)); ForEach(Array(rows.enumerated()), id: \.offset) { i, row in HStack(spacing: 0) { ForEach(Array(row.enumerated()), id: \.offset) { _, cell in Text(cell).font(.caption).frame(width: 150, alignment: .leading).padding(10) } }.background(i.isMultiple(of: 2) ? Color.clear : Color.white.opacity(0.02)) } }.dqCard(corner: 12) } }
    case .list(let title, let items): VStack(alignment: .leading, spacing: 6) { Text(title).font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.55)); ForEach(items, id: \.self) { Text("•  \($0)").bodyText() } }
    } }
}

struct CodeView: View {
    let language, code: String
    var body: some View { VStack(alignment: .leading, spacing: 6) { HStack { Text(language.uppercased()).font(.system(size: 10, weight: .bold, design: .monospaced)).foregroundStyle(DQ.text.opacity(0.4)); Spacer(); Button("Copy") { UIPasteboard.general.string = code }.font(.caption.bold()) }; ScrollView(.horizontal) { Text(code).font(.system(size: 12.5, design: .monospaced)).fixedSize(horizontal: true, vertical: false).padding(16) }.frame(maxWidth: .infinity, alignment: .leading).dqCard(corner: 12) } }
}
struct ReadingCard: View {
    let reading: FurtherReading; let action: () -> Void
    var body: some View { Button(action: action) { VStack(alignment: .leading, spacing: 4) { HStack { Text(reading.resourceType.replacingOccurrences(of: "_", with: " ").uppercased()).font(.caption2.bold()).foregroundStyle(DQ.blueLight); Text(reading.publisher).font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Text(reading.title).font(.subheadline.bold()); Text(reading.whyRead).bodyText(); Text("Open ↗").font(.caption.bold()).foregroundStyle(DQ.green).padding(.top, 2) }.frame(maxWidth: .infinity, alignment: .leading).padding(14).dqCard(corner: 12) }.buttonStyle(.plain) }
}

struct ChallengeView: View {
    @EnvironmentObject var model: AppModel; @EnvironmentObject var store: ProgressStore; let content: LoadedContent
    @State private var hintsShown = 0; @State private var showSolution = false
    var body: some View { if let challenge = content.challenge(model.nav.challengeId) { let done = store.progress.completedChallengeIds.contains(challenge.id)
        ScrollView { VStack(alignment: .leading, spacing: 0) { BackHeader(label: "Challenge · Optional").padding(.bottom, 14); Text(challenge.title).font(.system(size: 21, weight: .black)); Text("~\(challenge.estimatedMinutes) min practice · +\(challenge.rewards.xp) XP · \(challenge.rewards.stars)★").font(.caption).foregroundStyle(DQ.text.opacity(0.5)).padding(.vertical, 8); Text(challenge.prompt).bodyText().padding(.bottom, 20)
            section("Success criteria"); ForEach(challenge.successCriteria, id: \.self) { Text("✓  \($0)").bodyText().padding(.vertical, 3) }.padding(.bottom, 20)
            section("Starter code"); CodeView(language: challenge.starterCode.language, code: challenge.starterCode.code).padding(.bottom, 20)
            section("Hints"); ForEach(Array(challenge.hints.prefix(hintsShown).enumerated()), id: \.offset) { _, hint in Text("💡  \(hint)").bodyText().padding(12).dqCard(corner: 12, fill: DQ.amber.opacity(0.08)).padding(.bottom, 6) }; if hintsShown < challenge.hints.count { Button("Reveal hint \(hintsShown + 1) of \(challenge.hints.count) ›") { hintsShown += 1 }.font(.caption.bold()).foregroundStyle(DQ.amber).padding(.bottom, 20) }
            section("Solution outline"); if showSolution { ForEach(Array(challenge.solutionOutline.enumerated()), id: \.offset) { i, step in HStack(alignment: .top) { Text("\(i+1).").foregroundStyle(DQ.blue).bold(); Text(step).bodyText() }.padding(.vertical, 3) }.padding(.bottom, 20) } else { Button("Reveal solution outline (try it yourself first)") { showSolution = true }.font(.caption).foregroundStyle(DQ.text.opacity(0.55)).frame(maxWidth: .infinity).padding(16).dqCard(corner: 12).buttonStyle(.plain).padding(.bottom, 20) }
            section("Verification"); ForEach(challenge.verification, id: \.self) { Text("▸  \($0)").bodyText().padding(.vertical, 3) }.padding(.bottom, 22)
            if done { Text("✓ Challenge completed").font(.headline.bold()).foregroundStyle(DQ.green).frame(maxWidth: .infinity).padding(15).dqCard(corner: 14, fill: DQ.green.opacity(0.12)) } else { DQButton(title: "Mark Challenge Complete") { store.completeChallenge(challenge) } }
        }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 70) }.foregroundStyle(DQ.text)
    } }
    private func section(_ text: String) -> some View { SectionLabel(text: text).padding(.bottom, 10) }
}

struct ReviewView: View {
    @EnvironmentObject var model: AppModel; let content: LoadedContent
    var body: some View { if let state = model.review { VStack(alignment: .leading) { HStack { Button("←") { model.exitReview() }.font(.title3); VStack(alignment: .leading) { Text("DAILY REVIEW").font(.caption.bold()).foregroundStyle(DQ.blueLight); Text("\(min(state.index, state.recallItemIds.count)) of \(state.recallItemIds.count)").font(.caption2).foregroundStyle(DQ.text.opacity(0.45)) } }; if state.isComplete || content.recallItem(state.currentId) == nil { Spacer(); VStack(spacing: 10) { Text("Memory strengthened").font(.system(size: 20, weight: .black)).foregroundStyle(DQ.green); Text("You’re caught up for now. Come back when another idea is ready to revisit.").font(.subheadline).multilineTextAlignment(.center).foregroundStyle(DQ.text.opacity(0.6)); DQButton(title: "Back Home") { model.exitReview() }.padding(.top, 14) }; Spacer() } else if let item = content.recallItem(state.currentId) { Spacer().frame(height: 34); Text(item.lesson.title.uppercased()).font(.caption.bold()).foregroundStyle(DQ.text.opacity(0.4)); Text(item.recall.prompt).font(.system(size: 19, weight: .bold)).padding(.vertical, 12); TextField("Explain it from memory first…", text: Binding(get: { model.review?.answer ?? "" }, set: { model.review?.answer = $0 }), axis: .vertical).padding(15).dqCard(corner: 14).disabled(state.revealed); if state.revealed { VStack(alignment: .leading, spacing: 6) { Text("MODEL ANSWER").font(.caption.bold()).foregroundStyle(DQ.blueLight); Text(item.recall.answer).bodyText() }.padding(15).dqCard(corner: 14, fill: DQ.blue.opacity(0.1)).padding(.top, 16) }; Spacer(); if !state.revealed { DQButton(title: "Compare Answer", enabled: !state.answer.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty) { model.revealReview() } } else { Text("How well did you remember the idea?").font(.caption).foregroundStyle(DQ.text.opacity(0.55)).frame(maxWidth: .infinity); HStack { ForEach(ReviewRating.allCases, id: \.self) { rating in Button(rating.rawValue.capitalized) { model.rateCurrentReview(rating) }.font(.caption.bold()).frame(maxWidth: .infinity).padding(.vertical, 12).background((rating == .again ? DQ.amber : rating == .hard ? DQ.blueLight : DQ.green).opacity(0.16)).clipShape(RoundedRectangle(cornerRadius: 12)) } } } } }.padding(.horizontal, 20).padding(.vertical, 18).foregroundStyle(DQ.text) } }
}

extension Text { func bodyText() -> some View { self.font(.system(size: 13.5)).foregroundStyle(DQ.text.opacity(0.78)).lineSpacing(4) } }
