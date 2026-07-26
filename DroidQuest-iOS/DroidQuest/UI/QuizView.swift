import SwiftUI

struct QuizView: View {
    @EnvironmentObject var model: AppModel; let content: LoadedContent
    var body: some View { if let state = model.quiz, let quiz = content.quiz(state.quizId) { VStack(spacing: 0) {
        HStack(spacing: 12) { Button("✕") { model.exitQuiz() }.foregroundStyle(DQ.text.opacity(0.6)); HStack(spacing: 5) { ForEach(quiz.questions.indices, id: \.self) { i in Capsule().fill(i < state.index ? DQ.green : (i == state.index ? DQ.text.opacity(0.35) : DQ.text.opacity(0.1))).frame(height: 6) } }; Text("\(min(state.index, quiz.questions.count))/\(quiz.questions.count)").font(.caption.bold()).foregroundStyle(DQ.amber) }.padding(.bottom, 18)
        if state.phase == .done { QuizResultView(quiz: quiz, state: state) }
        else if let question = quiz.questions[safe: state.index] { ScrollView { VStack(alignment: .leading, spacing: 0) { Text("\(quiz.title) · \(question.type.label)".uppercased()).font(.caption.bold()).tracking(0.5).foregroundStyle(DQ.text.opacity(0.4)).padding(.bottom, 8); Text(question.prompt).font(.system(size: 17, weight: .bold)).lineSpacing(4).padding(.bottom, 16); QuestionInput(question: question, enabled: state.phase == .question).id(question.id); if state.phase == .feedback { FeedbackView(question: question, correct: state.lastCorrect).padding(.top, 16) } }.frame(maxWidth: .infinity, alignment: .leading) }
            Spacer().frame(height: 12); controls(question, state)
        }
    }.padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 28).foregroundStyle(DQ.text) } }

    @ViewBuilder private func controls(_ q: Question, _ state: QuizUIState) -> some View {
        if state.phase == .question { DQButton(title: "Check Answer", enabled: isAnswered(q, state.answers[q.id] ?? .none)) { model.submitCurrentQuestion() } }
        else if QuizEvaluator.requiresSelfAssessment(q) && state.lastCorrect == nil { Text("Compare the idea, not the exact wording.").font(.caption).foregroundStyle(DQ.text.opacity(0.55)).frame(maxWidth: .infinity).padding(.bottom, 8); HStack { DQButton(title: "Not yet", color: DQ.amber) { model.assessCurrentQuestion(false) }; DQButton(title: "I got the idea") { model.assessCurrentQuestion(true) } } }
        else { DQButton(title: state.index + 1 >= (content.quiz(state.quizId)?.questions.count ?? 0) ? "See Results" : "Next Question") { model.nextQuestion() } }
    }
    private func isAnswered(_ q: Question, _ answer: UserAnswer) -> Bool { switch answer { case .text(let v): return !v.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty; case .bool: return true; case .choices(let v): return !v.isEmpty; case .pairs(let v): return v.count >= q.answer.stringMap.count; case .none: return false } }
}

private struct QuestionInput: View {
    @EnvironmentObject var model: AppModel; let question: Question; let enabled: Bool
    @State private var order: [String] = []; @State private var pairs: [String: String] = [:]
    var answer: UserAnswer { model.quiz?.answers[question.id] ?? .none }
    var body: some View { Group { switch question.type {
    case .singleChoice: options(multiple: false)
    case .multipleChoice: options(multiple: true)
    case .trueFalse: HStack { option("True", selected: answer == .bool(true)) { model.setAnswer(.bool(true), questionId: question.id) }; option("False", selected: answer == .bool(false)) { model.setAnswer(.bool(false), questionId: question.id) } }
    case .fillBlank, .shortAnswer, .spotBug, .codeOutput: textInput
    case .orderSteps: orderInput
    case .matchPairs: pairInput
    } }.disabled(!enabled).onAppear { if question.type == .orderSteps && order.isEmpty { order = (question.options == nil ? question.answer.strings : QuizEvaluator.options(question)).shuffled(); model.setAnswer(.choices(order), questionId: question.id) } } }

    @ViewBuilder private func options(multiple: Bool) -> some View { VStack(spacing: 10) { ForEach(QuizEvaluator.options(question), id: \.self) { value in let selected = multiple ? selectedChoices.contains(value) : answer == .text(value); option(value, selected: selected, multi: multiple) { if multiple { var values = selectedChoices; if let index = values.firstIndex(of: value) { values.remove(at: index) } else { values.append(value) }; model.setAnswer(.choices(values), questionId: question.id) } else { model.setAnswer(.text(value), questionId: question.id) } } } } }
    private var selectedChoices: [String] { if case .choices(let values) = answer { values } else { [] } }
    private func option(_ text: String, selected: Bool, multi: Bool = false, action: @escaping () -> Void) -> some View { Button(action: action) { HStack { if multi { Image(systemName: selected ? "checkmark.square.fill" : "square").foregroundStyle(selected ? DQ.blue : DQ.text.opacity(0.3)) }; Text(text).font(.system(size: 14.5)); Spacer() }.padding(.horizontal, 16).padding(.vertical, 14).background(selected ? DQ.blue.opacity(0.16) : DQ.card).clipShape(RoundedRectangle(cornerRadius: 12)).overlay(RoundedRectangle(cornerRadius: 12).stroke(selected ? DQ.blue : Color.white.opacity(0.08), lineWidth: selected ? 1.5 : 1)) }.buttonStyle(.plain).frame(maxWidth: .infinity) }
    private var textInput: some View { VStack(alignment: .leading, spacing: 8) { if question.type == .codeOutput { Text("Enter what the program prints—not Kotlin code.").font(.caption.bold()).foregroundStyle(DQ.blueLight) }; TextField(question.type == .codeOutput ? "Type only the program output" : (question.type == .fillBlank ? "Type the missing text" : "Type your answer"), text: Binding(get: { if case .text(let value) = answer { value } else { "" } }, set: { model.setAnswer(.text($0), questionId: question.id) }), axis: .vertical).font(question.type == .codeOutput ? .system(size: 14, design: .monospaced) : .system(size: 14)).padding(14).dqCard(corner: 12) } }
    private var orderInput: some View { VStack(spacing: 8) { ForEach(order.indices, id: \.self) { i in HStack { Text("\(i + 1)").font(.caption.bold()).foregroundStyle(DQ.blueLight).frame(width: 22, height: 22).background(DQ.blue.opacity(0.2)).clipShape(Circle()); Text(order[i]).font(.system(size: 13, design: .monospaced)); Spacer(); Button("↑") { order.swapAt(i, i-1); model.setAnswer(.choices(order), questionId: question.id) }.disabled(i == 0); Button("↓") { order.swapAt(i, i+1); model.setAnswer(.choices(order), questionId: question.id) }.disabled(i == order.count - 1) }.padding(12).dqCard(corner: 12) } } }
    private var pairInput: some View { VStack(spacing: 12) { ForEach(Array(question.answer.stringMap.keys), id: \.self) { left in VStack(alignment: .leading, spacing: 8) { Text(left).font(.subheadline.bold()); ScrollView(.horizontal, showsIndicators: false) { HStack { ForEach(Array(question.answer.stringMap.values).shuffled(seed: question.id), id: \.self) { right in Button(right) { pairs[left] = right; model.setAnswer(.pairs(pairs), questionId: question.id) }.font(.system(size: 12.5, weight: .semibold, design: .monospaced)).foregroundStyle(pairs[left] == right ? DQ.ink : DQ.text.opacity(0.7)).padding(.horizontal, 12).padding(.vertical, 7).background(pairs[left] == right ? DQ.blue : DQ.text.opacity(0.08)).clipShape(Capsule()).buttonStyle(.plain) } } } }.padding(12).dqCard(corner: 12) } } }
}

private struct FeedbackView: View {
    let question: Question; let correct: Bool?
    var color: Color { correct == true ? DQ.green : (correct == false ? DQ.amber : DQ.blueLight) }
    var body: some View { VStack(alignment: .leading, spacing: 6) { Text(correct == true ? "You’ve got it" : (correct == false ? "Let’s learn from this one" : "Compare with the model answer")).font(.subheadline.bold()).foregroundStyle(color); if correct != true { Text(correct == nil ? "Model answer" : "Expected answer").font(.caption.bold()).foregroundStyle(color); Text(QuizEvaluator.modelAnswer(question)).font(question.type == .codeOutput || question.type == .fillBlank ? .system(size: 13, design: .monospaced) : .system(size: 13)).padding(.bottom, 4) }; Text(question.explanation).bodyText() }.padding(16).frame(maxWidth: .infinity, alignment: .leading).dqCard(corner: 14, fill: color.opacity(0.12)) }
}

private struct QuizResultView: View {
    @EnvironmentObject var model: AppModel; let quiz: Quiz; let state: QuizUIState
    var body: some View { let passed = state.score?.passed == true
        VStack(spacing: 10) { Spacer(); Text(passed ? "✓" : "✕").font(.system(size: 44)).foregroundStyle(passed ? DQ.green : DQ.red); Text("\(state.score?.correct ?? 0) / \(state.score?.total ?? quiz.questions.count) correct").font(.system(size: 20, weight: .black)); Text(passed ? "Passed · needed \(Int(quiz.passingScore * 100))%" : "You’re learning · needed \(Int(quiz.passingScore * 100))%").font(.subheadline.bold()).foregroundStyle(passed ? DQ.green : DQ.text.opacity(0.55)); if state.recorded?.firstPass == true { Text("+\(state.recorded!.outcome.xpAwarded) XP · \(state.recorded!.outcome.starsAwarded)★ earned").font(.subheadline.bold()).foregroundStyle(DQ.amber) } else if passed { Text("Completed before — rewards granted earlier").font(.caption).foregroundStyle(DQ.text.opacity(0.45)) }; Spacer().frame(height: 16); if !passed { Text("Mistakes are part of practice. You’ve now seen the model answers, so try once more when you’re ready.").font(.subheadline).multilineTextAlignment(.center).foregroundStyle(DQ.text.opacity(0.6)); DQButton(title: "Try Again") { model.retryQuiz() }; Button("Back to Map") { model.exitQuiz() }.font(.subheadline.bold()).padding(14) } else { DQButton(title: "Back to Map") { model.exitQuiz() } }; Spacer() }.frame(maxWidth: .infinity)
    }
}

private extension QuestionType { var label: String { rawValue.replacingOccurrences(of: "_", with: " ") } }
private extension Array where Element == String {
    func shuffled(seed: String) -> [String] { sorted { ("\(seed)\($0)".hashValue) < ("\(seed)\($1)".hashValue) } }
}
