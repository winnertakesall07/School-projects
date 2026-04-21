/**
 * Question - Represents a single Jeopardy question and its answer.
 *
 * To customize questions, open JeopardyGameV2.java and edit the
 * initializeQuestions() method. Each question is created with:
 *
 *   new Question("Your question text here", "The correct answer here")
 */
public class Question {

    private String questionText;
    private String answer;

    public Question(String questionText, String answer) {
        this.questionText = questionText;
        this.answer = answer;
    }

    public String getQuestion() {
        return questionText;
    }

    public String getAnswer() {
        return answer;
    }
}
