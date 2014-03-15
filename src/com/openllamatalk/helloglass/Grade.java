public class Grade {
  /**
   * Output report card grade.
   */
  public static void main(String[] args) {
    int total_sen = Integer.parseInt(args[0]);
    int grammar_errors = Integer.parseInt(args[1]);
    int fillers = Integer.parseInt(args[2]);
    String grade = getReportCardGrade(total_sen, grammar_errors, fillers);
    System.out.println(grade);
  }

  private static String getReportCardGrade(int total_sen, int grammar_errors,
                                    int fillers) {
    double grade;
    double prevalenceRate = 0.3;

    // Gain a rough estimate of the word count
    // This also has the positive side effect of rewarding sentence complexity.
    int totalWords = (int) ((double) total_sen / prevalenceRate);

    // Count grammar errors as 2 errors because they arise as problems
    // between at least two words.
    grade = 2.0 * (double) grammar_errors;
    // Vocal fillers are a single error each.
    grade += (double) fillers;
    // But fillers also reduce relative sentence length.
    totalWords -= fillers;

    // Compute grade as a percentage.
    grade = totalWords - grade;
    grade /= (double) totalWords;
    // Convert to a letter grade using social conventions.
    if (grade > .9) {
      return "A";
    } else if (grade > .8) {
      return "B";
    } else if (grade > .7) {
      return "C";
    } else if (grade > .6) {
      return "D";
    } else {
      return "F";
    }
  }

}
