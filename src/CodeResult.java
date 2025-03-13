import java.util.ArrayList;
import java.util.List;

public class CodeResult
{
    public String problemId;
    public CodeResult(String problemId)
    {
        this.problemId = problemId;
    }
    public String resultState;
    public int score;
    public String submissionTime;
    public List<TestCaseResult> testCaseResultList=new ArrayList<>();
}
