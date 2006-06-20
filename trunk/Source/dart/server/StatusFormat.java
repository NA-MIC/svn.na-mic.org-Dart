package dart.server;

import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

import org.apache.log4j.Logger;

/**
 * Format class that converts a numeric representation of a test
 * status to a string.
 *
 *   status = 1 maps to "Passed"
 *   status = 0 maps to "Failed"
 *   status = -1 maps to "Not run"
 *
 * This class is used when labeling axes in plots of test status. Test
 * status is stored in Dart as "p", "f", "n", or "m" (meta).  To plot
 * test status over time, jfreechart needs these status codes
 * converted to numbers, where we arbitrarily picked the mapping to be
 * (n = -1, f = 0, p = 1).  When jfreechart labels the range axis, we
 * tell it to use this class (which is subclass of
 * java.text.NumberFormat) to convert the values (-1, 0, 1) back into
 * strings ("Not run", "Failed", "Passed").
 *
 */
public class StatusFormat extends NumberFormat {

  /**
   * Method to format an integral number.
   */
  public StringBuffer format(long number, StringBuffer toAppendTo,
                             FieldPosition pos) {
    if (number == 1) {
      return toAppendTo.append("Passed");
    } else if (number == 0) {
      return toAppendTo.append("Failed");
    } else if (number == -1) {
      return toAppendTo.append("Not run");
    }

    return toAppendTo;
  }

  /**
   * Method to format a floating point number
   */
  public StringBuffer format(double number, StringBuffer toAppendTo,
                             FieldPosition pos) {
    if (number == 1.0) {
      return toAppendTo.append("Passed");
    } else if (number == 0.0) {
      return toAppendTo.append("Failed");
    } else if (number == -1.0) {
      return toAppendTo.append("Not run");
    }

    return toAppendTo;
  }

  /**
   * Method required for parsing a number from a string.
   * Dart does not use this method but the implementation is
   * required. Simply calls the superclass' method.  If we need to
   * parse the strings "Not run", "Failed", "Passed" into numeric
   * codes (-1, 0, 1) then we will need to change this implementation.
   */
  public Number parse(String source, ParsePosition parsePosition) {
    NumberFormat fmt = NumberFormat.getNumberInstance();
    return fmt.parse(source, parsePosition);
  }
  
}
