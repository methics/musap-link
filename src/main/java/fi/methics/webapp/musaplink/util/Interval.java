package fi.methics.webapp.musaplink.util;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Date;


/**
 * Intervals (delta time) are specified
 * using a concatenation of: numbers suffixed with
 * 's', 'm', 'h', or 'd', 'w', 'mon', 'y' modifiers
 * designating the  number  as  a second, minute,
 * hour, day, month or year value.  For example: 1h5m20s.
 * <p>
 * <ul>
 *  <li>Year:  a, y, year, years (365 days)</li>
 *  <li>Month: mon, month, months (30 days)</li>
 *  <li>Week: w, week, weeks (7 days)</li>
 *  <li>Day: d, day, days (24 hours)</li>
 *  <li>Hour: h, hour, hours (60 minutes)</li>
 *  <li>Minute: m, min, mins, minute, minutes (60 seconds)</li>
 *  <li>Second: s, sec, secs, second, seconds (1000 milliseconds)</li>
 *  <li>Millisecond: ms, msec, misecs, millis, millisec, millisecs, millisecond, milliseconds</li>
 *  <li>Typeless: An integer without unit suffix will use supplied default unit, which depends on call case.</li>
 * </ul>
 * <p>
 *
 * The input string can have intermediate spaces in it,
 * they are ignored at while parsing, and are there
 * to permit a bit more readable input syntax:
 * <p>
 *
 *  5d 3h 5s
 * <p>
 *
 * Result value of parseInterval() is in <i>milliseconds</i>,
 * and thus if application needs to have it in for
 * example as: hours, the application must do
 * conversion.
 *
 */



public class Interval implements Comparable<Interval> {

    protected static SecureRandom random = new SecureRandom();
    
    public static final int  SEC_MSECS    = 1000;
    public static final int  MINUTE_MSECS = 60*SEC_MSECS;
    public static final int  HOUR_MSECS   = 60*MINUTE_MSECS;
    public static final int  DAY_MSECS    = 24*HOUR_MSECS;
    public static final long WEEK_MSECS   = 7L*DAY_MSECS;
    public static final long MONTH_MSECS  = 30L*DAY_MSECS;
    public static final long YEAR_MSECS   = 365L*DAY_MSECS;

    public static final Interval ZERO = new Interval(0);

    private long val;
    
    private Interval(long v) {
        this.val = v;
    }

    /**
     * Get primitive internal value of this {@link Interval} in {@code long} milliseconds
     * <p>Use {@link #toIntMillis()} to get value as {@code int} instead.
     * @return internal primitive value in milliseconds
     */
    public long getValue() {
        return this.val;
    }

    /**
     * Compare parameter value to internal interval value.
     * <p>
     * This is alike the {@link #compareTo(Interval)},
     * but takes primitive long of milliseconds.
     * 
     * @param v an interval value in milliseconds.
     * @return 0 for equal values,
     *        -1 for this value being less than parameter value,
     *        +1 for this value being greater than parameter value.
     */
    public int compareTo(final long v) {
        final long c = (this.val - v);
        if (c == 0) return 0;
        if (c < 0) return -1;
        return 1;
    }
    
    /**
     * Compare parameter value to internal interval value.
     * 
     * @param v comparison target
     * @return 0 for equal,
     *        -1 for internal being less than parameter,
     *        +1 for internal being greater than parameter.
     * @throws NullPointerException if parameter v is null
     */
    @Override
    public int compareTo(final Interval v) {
        return this.compareTo(v.getValue());
    }


    @Override
    public boolean equals(final Object o) {
        if (o instanceof Interval) {
            Interval v = (Interval)o;
            return this.val == v.val;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.val);
    }

    /**
     * Convert internal value to integer seconds.
     * 
     * @return int of seconds
     */
    public int toSeconds() {
        return (int) Duration.ofMillis(this.val).getSeconds();
    }

    /**
     * Return milliseconds that fit a primitive int.
     * <p>Use {@link #getValue()} to get value as {@code long} instead.
     * @return milliseconds (integer)
     */
    public int toIntMillis() {
        return (int) this.val;
    }

    /**
     * Return milliseconds.
     * <p>Use {@link #toIntMillis()} to get value as primitive int instead.
     * @return milliseconds
     */
    public long toMillis() {
        return this.val;
    }
    
    /**
     * Convert internal value to integer minutes.
     * 
     * @return int of minutes
     */
    public int toMinutes() {
        return (int) Duration.ofMillis(this.val).toMinutes();
    }
    
    /**
     * Convert internal value to integer hours.
     * 
     * @return int of hours
     */
    public int toHours() {
        return (int) Duration.ofMillis(this.val).toHours();
    }

    /**
     * Convert internal value to integer days.
     * 
     * @return int of days
     */
    public int toDays() {
        return (int) Duration.ofMillis(this.val).toDays();
    }
    
    /**
     * Produce java.util.Date that is computed by System.currentTimeMillis() + this.value
     * 
     * @return Date containing current time + this.value
     */
    public Date toDate() {
        return new Date(System.currentTimeMillis() + this.val);
    }

    /**
     * Produce java.sql.Timestamp that is computed by System.currentTimeMillis() + this.value
     * 
     * @return Timestamp containing current time + this.value
     */
    public Timestamp toTimestamp() {
        return new Timestamp(System.currentTimeMillis() + this.val);
    }
    
    /**
     * Convert this to a {@link Duration}
     * <p>This can be helpful with for example {@link Instant#plus(TemporalAmount)}
     * @return Duration
     */
    public Duration toDuration() {
        return Duration.ofMillis(this.toIntMillis());
    }
    
    /**
     * Produce java.time.Instant that is computed by System.currentTimeMillis() + this.value
     * 
     * @return Instant containing current time + this.value
     */
    public Instant toInstant() {
        return this.toDate().toInstant();
    }
    
    /**
     * Produce ISO-8601 String that is computed by System.currentTimeMillis() + this.value
     * @return ISO-8601 String
     */
    public String toIso8601() {
        return this.toInstant().toString();
    }

    /**
     * Interval parser.
     * Untyped unit size is 1 millisecond.
     * 
     * @return Interval object
     */
    public static Interval parse(String input)
        throws NumberFormatException {
        return parse(input, 1); // untyped value is in milliseconds
    }

    /**
     * Interval parser.
     * Untyped unit size is 1 second.
     * 
     * @return Interval object
     */
    public static Interval parseSeconds(final String input)
        throws NumberFormatException {
        return parse(input, 1000); // untyped value is in seconds
    }

    /**
     * Internal implementation of parseInterval()/parseIntervalSeconds()
     * 
     * @param input String input, possibly containing the unit. Must start with a parseable number value.
     * @param multiplier Multiplier to use if no unit can be determined from the input String. (use 1000 for seconds, for example)
     * @return Interval object
     * @throws NumberFormatException if input is null or contains an unknown suffix
     */
    private static Interval parse(final String input, final int multiplier)
        throws NumberFormatException
    {
        if (input == null) {
            throw new NumberFormatException("Unable to parse empty value as interval");
        }
        
        // First parse ISO-8601 duration
        if (input.startsWith("P")) {
            try {
                return Interval.ofSeconds(Duration.parse(input).getSeconds());
            } catch (Exception e) {
                // OK - parse normally
            }
        }
        
        long  intvl = 0;
        long    val;
        boolean negative = false;

        int i;
        int l = input.length();
        for (i = 0; i < l; ++i) {
            char c = input.charAt(i);

            if (c == ' ') {
                continue; // Leading spaces are ignored
            }
            if (c == '-') negative = true;

            val = 0;
            boolean isHex = false;
            int radix = 10;
            if (c == '0') {
                // Maybe hex prefix "0x"
                ++i;
                if (i < l) {
                    c = input.charAt(i);
                }
                if (c == 'x' || c == 'X') {
                    isHex = true;
                    radix = 16;
                    ++i;
                    if (i < l) {
                        c = input.charAt(i);
                    }
                }
            }
            int v;
            while ( i < l && (v = digit(c, isHex)) >= 0) {
                val = addAndCheck(mulAndCheck(val, radix),  v);
                ++i;
                if (i < l) {
                    c = input.charAt(i);
                }
            }

            while (c == ' ') { // ignore intermediate spaces
                ++i;
                if (i < l) {
                    c = input.charAt(i);
                } else {
                    break;
                }
            }

            String name = "";
            while ((i < l) &&
                   (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'))) {
                name += c;
                ++i;
                if (i < l) {
                    c = input.charAt(i);
                } else {
                    break;
                }
            }

            // Integer Overlow Warning: 30 days is more than 2^31 milliseconds!

            if (name.equalsIgnoreCase("a") ||
                name.equalsIgnoreCase("y") ||
                name.equalsIgnoreCase("year") ||
                name.equalsIgnoreCase("years")) {
                val = mulAndCheck(val, YEAR_MSECS);
            } else if (name.equalsIgnoreCase("mon") ||
                       name.equalsIgnoreCase("month") ||
                       name.equalsIgnoreCase("months")) {
                val = mulAndCheck(val, MONTH_MSECS);
            } else if (name.equalsIgnoreCase("w") ||
                       name.equalsIgnoreCase("week") ||
                       name.equalsIgnoreCase("weeks")) {
                val = mulAndCheck(val, WEEK_MSECS);
            } else if (name.equalsIgnoreCase("d") ||
                       name.equalsIgnoreCase("day") ||
                       name.equalsIgnoreCase("days")) {
                val = mulAndCheck(val, DAY_MSECS);
            } else if (name.equalsIgnoreCase("h") ||
                       name.equalsIgnoreCase("hour") ||
                       name.equalsIgnoreCase("hours")) {
                val = mulAndCheck(val, HOUR_MSECS);
            } else if (name.equalsIgnoreCase("m") ||
                       name.equalsIgnoreCase("min") ||
                       name.equalsIgnoreCase("mins") ||
                       name.equalsIgnoreCase("minute") ||
                       name.equalsIgnoreCase("minutes")) {
                val = mulAndCheck(val, MINUTE_MSECS);
            } else if (name.equalsIgnoreCase("s") ||
                       name.equalsIgnoreCase("sec") ||
                       name.equalsIgnoreCase("secs") ||
                       name.equalsIgnoreCase("second") ||
                       name.equalsIgnoreCase("seconds")) {
                val = mulAndCheck(val, SEC_MSECS);
            } else if (name.equalsIgnoreCase("ms") ||
                       name.equalsIgnoreCase("msec") ||
                       name.equalsIgnoreCase("msecs") ||
                       name.equalsIgnoreCase("millis") ||
                       name.equalsIgnoreCase("millisec") ||
                       name.equalsIgnoreCase("millisecs") ||
                       name.equalsIgnoreCase("millisecond") ||
                       name.equalsIgnoreCase("milliseconds")) {
                // multiplier == 1
            } else if (name.equals("")) {
                // "Typeless" value
                val = mulAndCheck(val, multiplier);
            } else {
                throw new NumberFormatException("Unrecognized time interval input at or preceding of index " + (i) +
                                                " on: '" + input + "'");
            }
            intvl = addAndCheck(intvl, val);
        }
        
        if (negative) {
            intvl = -intvl;
        }
        
        return new Interval(intvl);
    }

    /**
     * Multiply two long integers, checking for overflow.
     * 
     * @param a first value
     * @param b second value
     * @return the product <code>a * b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *         long
     * @since 1.2 -- org.apache.commons.math3.util.ArithmeticUtils
     */
    public static long mulAndCheck(long a, long b) throws ArithmeticException {
        long ret;
        String msg = "overflow: multiply";
        if (a > b) {
            // use symmetry to reduce boundry cases
            ret = mulAndCheck(b, a);
        } else {
            if (a < 0) {
                if (b < 0) {
                    // check for positive overflow with negative a, negative b
                    if (a >= Long.MAX_VALUE / b) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);
                    }
                } else if (b > 0) {
                    // check for negative overflow with negative a, positive b
                    if (Long.MIN_VALUE / b <= a) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);
                        
                    }
                } else {
                    // assert b == 0
                    ret = 0;
                }
            } else if (a > 0) {
                // assert a > 0
                // assert b > 0
                
                // check for positive overflow with positive a, positive b
                if (a <= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new ArithmeticException(msg);
                }
            } else {
                // assert a == 0
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a Addend.
     * @param b Addend.
     * @return the sum {@code a + b}.
     * @throws ArithmeticException if the result cannot be represented as a {@code long}.
     * @since 1.2 -- org.apache.commons.math3.util.ArithmeticUtils
     */
     public static long addAndCheck(long a, long b) throws ArithmeticException {
         final long result = a + b;
         if (!((a ^ b) < 0 | (a ^ result) >= 0)) {
             throw new ArithmeticException("Addition overflow detected");
         }
         return result;
    }

    private static int digit(char c, boolean isHex) {
        // Decimal digit?
        if ('0' <= c && c <= '9') return c - '0';
        if (isHex) {
            // hex space codes allowed producing values 10..15
            if ('A' <= c && c <= 'F') return c - 'A' + 10;
            if ('a' <= c && c <= 'f') return c - 'a' + 10;
        }
        // Not a digit
        return -1;
    }

    static public String toString(long interval) {
        String s = "";
        long l;

        if (interval == 0) {
            s = "0 milliseconds";
        }
        
        // Milliseconds ?
        l = interval % 1000;
        if (l != 0) {
            if (l == 1) {
                s = l+" millisecond";
            } else {
                s = l+" milliseconds";
            }
        }
        interval /= 1000;

        // Seconds ?
        l = interval % 60;
        if (l != 0) {
            if (l == 1) {
                s = l+" second "+s;
            } else {
                s = l+" seconds "+s;
            }
        }
        interval /= 60;

        // Minutes ?
        l = interval % 60;
        if (l != 0) {
            if (l == 1) {
                s = l+" minute "+s;
            } else {
                s = l+" minutes "+s;
            }
        }
        interval /= 60;

        // Hours ?
        l = interval % 24;
        if (l != 0) {
            if (l == 1) {
                s = l+" hour "+s;
            } else {
                s = l+" hours "+s;
            }
        }
        interval /= 24;

        if (interval != 0) {
            if (interval == 1) {
                s = interval+" day "+s;
            } else {
                s = interval+" days "+s;
            }
        }

        return s;
    }


    public static void main(String[] args) {
        System.out.println("Interval: '"+args[0]+"' iv= "+parse(args[0]));
    }
    
    /**
     * Return  JDBC Timestamp(currentTimeMillis() - this.value)
     * 
     * @return
     */
    public Timestamp getNowMinusTimestamp() {
        return new Timestamp(this.nowMinus());
    }

    /**
     * Return  JDBC Timestamp(currentTimeMillis() - this.value + offsetMillis)
     * 
     * @param offsetMillis 
     * @return
     */
    public Timestamp getNowMinusTimestamp(final int offsetMillis) {
        return new Timestamp(this.nowMinus(offsetMillis));
    }

    /**
     * Calculate System.currentTimeMillis() + this.value
     * 
     * @return
     */
    public long nowPlus() {
        return this.val + System.currentTimeMillis();
    }

    /**
     * Calculate System.currentTimeMillis() - this.value
     * 
     * @return
     */
    public long nowMinus() {
        return System.currentTimeMillis() - this.val;
    }

    /**
     * Calculate System.currentTimeMillis() - this.value + offsetMillis
     * 
     * @param offsetMillis 
     * @return
     */
    public long nowMinus(final int offsetMillis) {
        return System.currentTimeMillis() - this.val + offsetMillis;
    }

    /**
     * Enforce minimum value <i>i</i>.
     * <p>
     * <b>Modifies this object</b>
     * 
     * @param i
     * @return true if enforcement occurred.
     */
    public boolean enforceMin(int i) {
        if (this.val < i) {
            this.val = i;
            return true;
        }
        return false;
    }

    /**
     * Enforce minimum value <i>i</i>.
     * <p>
     * <b>Modifies this object</b>
     * 
     * @param i minimum interval value. Null is ignored returning false.
     * @return true if enforcement occurred.
     */
    public boolean enforceMin(final Interval i) {
        if (i != null && this.val < i.val) {
            this.val = i.val;
            return true;
        }
        return false;
    }

    /**
     * Enforce maximum value <i>i</i>.
     * <p>
     * <b>Modifies this object</b>
     * @param i
     * @return true if enforcement occurred.
     */
    public boolean enforceMax(int i) {
        if (this.val > i) {
            this.val = i;
            return true;
        }
        return false;
    }

    /**
     * Enforce maximum value <i>i</i>.
     * <p>
     * <b>Modifies this object</b>
     *
     * @param i maximum interval value. Null is ignored returning false.
     * @return true if enforcement occurred.
     */
    public boolean enforceMax(final Interval i) {
        if (i != null && this.val > i.val) {
            this.val = i.val;
            return true;
        }
        return false;
    }

    /**
     * Indicate if value is greater than zero.
     * 
     * @return true for positive value
     */
    public boolean isPositive() {
        return this.val > 0;
    }


    /**
     * Indicate if value is greater or equal than zero.
     * 
     * @return true for positive or zero value
     */
    public boolean isNotNegative() {
        return this.val >= 0;
    }
    
    /**
     * Is the interval value 0?
     * @return true if {@code getValue() == 0}
     */
    public boolean isZero() {
        return this.val == 0;
    }
    
    /**
     * Sleep for the interval time.
     * Ignores InterruptedException.
     */
    public void sleep() {
        try {
            if (this.isPositive()) {
                Thread.sleep(this.val);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Add an interval to another
     * @param other Other interval
     * @return returns a new {@link Interval} with combined value
     */
    public Interval plus(Interval other) {
        long ourMillis   = this.getValue();
        long theirMillis = other.getValue();
        return new Interval(ourMillis+theirMillis);
    }


    /**
     * Add an interval to another
     * @param theirMillis Other interval
     * @return returns a new {@link Interval} with combined value
     */
    public Interval plus(long theirMillis) {
        long ourMillis   = this.getValue();
        return new Interval(ourMillis+theirMillis);
    }
    
    
    
    @Override
    public String toString() {
        return toString(this.val);
    }
    
    /**
     * Create a new Interval from milliseconds
     * @param millis Interval in milliseconds
     * @return new Interval
     */
    public static Interval ofMillis(final long millis) {
        return new Interval(millis);
    }
    
    /**
     * Create a new Interval from seconds
     * @param seconds Interval in seconds
     * @return new Interval
     */
    public static Interval ofSeconds(final long seconds) {
        return new Interval(Duration.ofSeconds(seconds).toMillis());
    }
    
    /**
     * Create a new Interval from seconds
     * @param seconds Interval in seconds
     * @return new Interval
     */
    public static Interval ofSeconds(final Integer seconds) {
        if (seconds == null) return new Interval(0); // Avoid NPE
        return new Interval(Duration.ofSeconds(seconds.intValue()).toMillis());
    }
    
    /**
     * Create a new Interval from minutes
     * @param minutes Interval in minutes
     * @return new Interval
     */
    public static Interval ofMinutes(final int minutes) {
        return new Interval(Duration.ofMinutes(minutes).toMillis());
    }
    
    /**
     * Create a new Interval from hours
     * @param hours Interval in hours
     * @return new Interval
     */
    public static Interval ofHours(final int hours) {
        return new Interval(Duration.ofHours(hours).toMillis());
    }
    
    /**
     * Create a new Interval from days
     * @param days Interval in days
     * @return new Interval
     */
    public static Interval ofDays(final int days) {
        return new Interval(Duration.ofDays(days).toMillis());
    }
    
    /**
     * Create a new Interval from months
     * @param months Interval in months
     * @return new Interval
     */
    public static Interval ofMonths(final int months) {
        return Interval.ofDays(31*months);
    }
    
    /**
     * Create a new Interval from years
     * @param years Interval in years
     * @return new Interval
     */
    public static Interval ofYears(final int years) {
        return Interval.ofDays(Period.ofYears(years).getDays());
    }
    
    /**
     * Create a new Interval from weeks
     * @param years Interval in weeks
     * @return new Interval
     */
    public static Interval ofWeeks(final int years) {
        return Interval.ofDays(Period.ofWeeks(years).getDays());
    }
    
    /**
     * Get a non null Interval value
     * @param interval Interval (which may be null)
     * @return Interval value or {@link Interval#ZERO} if given interval was null
     */
    public static Interval valueOf(final Interval interval) {
        if (interval == null) return Interval.ZERO;
        return Interval.ofMillis(interval.getValue());
    }
    
    /**
     * Get a non null Interval value
     * @param interval String to parse (which may be null)
     * @return Interval value or {@link Interval#ZERO} if given interval was null or unparseable
     */
    public static Interval valueOf(final String interval) {
        if (interval == null) return Interval.ZERO;
        try {
            return Interval.parse(interval);
        } catch (NumberFormatException e) {
            return Interval.ZERO;
        }
    }
    
    /**
     * Get an interval of random time between given min and max
     * @param min Minimum milliseconds
     * @param max Maximum milliseconds
     * @return Interval
     */
    public static Interval ofRandomMillis(final int min, final int max) {
        int diff = max-min;
        int rand = random.nextInt(diff);
        return Interval.ofMillis(min+rand);
    }

    /**
     * Get longer of two intervals 
     */
    public Interval max(Interval other) {
        if (this.val >= other.val) return this;
        return other;
    }

    /**
     * Get shorter of two intervals 
     */
    public Interval min(Interval other) {
        if (this.val <= other.val) return this;
        return other;
    }

    /**
     * Get shorter of two intervals 
     */
    public Interval min(long othermillis) {
        if (this.val <= othermillis) return this;
        return Interval.ofMillis(othermillis);
    }

    /**
     * Multiply the interval
     */
    public Interval mult(double multiplier) {
        return new Interval((long) (this.val * multiplier));
    }
}
