package com.backend.escort.helpers;

import com.backend.escort.model.Driver;
import com.backend.escort.model.Emergency;
import com.backend.escort.model.Student;
import com.backend.escort.model.Trip;
import com.backend.escort.payload.request.DriverRequest;
import com.backend.escort.payload.request.StudentRequest;
import com.backend.escort.repository.EmergencyRepository;
import com.backend.escort.repository.TripRepository;
import com.backend.escort.security.service.ImageStorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import java.io.IOException;

import java.security.SecureRandom;
import java.text.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.time.DayOfWeek.*;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Service
public class Helper {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    TripRepository tripRepository;
    @Autowired
    EmergencyRepository emergencyRepository;
    @Autowired
    ImageStorageService imageStorageService;

    // The following attributes allow us to send emails using Velocity templates

    private static final String CHARSET_UTF8 = "UTF-8";

    /*
     * The following methods generates a random password  of length
     * When the driver is registered by the admin
     */
    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder();
        /*
         * The password will contain the following characters:
         * [A-Z][a-z][0-9]
         */
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            password.append(characters.charAt(randomIndex));
        }
        return password.toString();
    }

    /*
     * Generates a channel name from a username
     * The driver will be able to join the channel
     */

    public static String generateChannelName(String username) {
        StringBuilder channel = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(username.length());
            channel.append(username.charAt(randomIndex));
        }
        return channel.toString();
    }

    // Send driver email after admin adds them to the system
    public void sendMail(String username, String organisation, String email, String password) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setTo(email);
                messageHelper.setSubject("Account activation");

                String emailContent = emailTemplate(username, organisation, email, password);
                messageHelper.setText(emailContent, true);
            }
        };
        javaMailSender.send(preparator);
    }


    private String emailTemplate(String username, String organisation, String email, String password) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html\n" +
                "  xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
                "  xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n" +
                "  xmlns:v=\"urn:schemas-microsoft-com:vml\"\n" +
                "  lang=\"en\"\n" +
                ">\n" +
                "  <head>\n" +
                "    <link\n" +
                "      rel=\"stylesheet\"\n" +
                "      type=\"text/css\"\n" +
                "      hs-webfonts=\"true\"\n" +
                "      href=\"https://fonts.googleapis.com/css?family=Lato|Lato:i,b,bi\"\n" +
                "    />\n" +
                "    <title>Email </title>\n" +
                "    <meta property=\"og:title\" content=\"Email \" />\n" +
                "\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "    <style type=\"text/css\">\n" +
                "      a {\n" +
                "        text-decoration: underline;\n" +
                "        color: inherit;\n" +
                "        font-weight: bold;\n" +
                "        color: #253342;\n" +
                "      }\n" +
                "\n" +
                "      h1 {\n" +
                "        font-size: 56px;\n" +
                "      }\n" +
                "\n" +
                "      h2 {\n" +
                "        font-size: 28px;\n" +
                "        font-weight: 900;\n" +
                "      }\n" +
                "\n" +
                "      p {\n" +
                "        font-weight: 100;\n" +
                "      }\n" +
                "\n" +
                "      td {\n" +
                "        vertical-align: top;\n" +
                "      }\n" +
                "\n" +
                "      #email {\n" +
                "        margin: auto;\n" +
                "        width: 600px;\n" +
                "        background-color: white;\n" +
                "      }\n" +
                "\n" +
                "      button {\n" +
                "        font: inherit;\n" +
                "        background-color: #212638;\n" +
                "        border: none;\n" +
                "        padding: 10px;\n" +
                "        text-transform: uppercase;\n" +
                "        letter-spacing: 2px;\n" +
                "        font-weight: 900;\n" +
                "        color: white;\n" +
                "        border-radius: 5px;\n" +
                "        box-shadow: 3px 3px #cbd6e2;\n" +
                "      }\n" +
                "\n" +
                "      .subtle-link {\n" +
                "        font-size: 9px;\n" +
                "        text-transform: uppercase;\n" +
                "        letter-spacing: 1px;\n" +
                "        color: #cbd6e2;\n" +
                "      }\n" +
                "    </style>\n" +
                "  </head>\n" +
                "\n" +
                "  <body\n" +
                "    bgcolor=\"#F5F8FA\"\n" +
                "    style=\"\n" +
                "      width: 100%;\n" +
                "      margin: auto 0;\n" +
                "      padding: 0;\n" +
                "      font-family: Lato, sans-serif;\n" +
                "      font-size: 18px;\n" +
                "      color: #33475b;\n" +
                "      word-break: break-word;\n" +
                "    \"\n" +
                "  >\n" +
                "    <div id=\"email\">\n" +
                "      <table role=\"presentation\" width=\"100%\">\n" +
                "        <tr>\n" +
                "          <td bgcolor=\"#212638\" align=\"center\" style=\"color: white\">\n" +
                "            <img\n" +
                "              alt=\"Logo\"\n" +
                "              src=\"https://i.postimg.cc/XqTR7HmM/unisafe-logo.png\"\n" +
                "              width=\"400px\"\n" +
                "              align=\"middle\"\n" +
                "            />\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "\n" +
                "      <table\n" +
                "        role=\"presentation\"\n" +
                "        border=\"0\"\n" +
                "        cellpadding=\"0\"\n" +
                "        cellspacing=\"5px\"\n" +
                "        width=\"100%\"\n" +
                "        style=\"padding: 5px 5px 5px 10px\"\n" +
                "      >\n" +
                "        <tr>\n" +
                "          <td>\n" +
                "            <h2>Welcome to UniSafe</h2>\n" +
                "            <p>\n" +
                "              Hello," + username + ". Your help is required at " + organisation + ". To use the mobile application, please provide the following credentials:</p>\n" +
                "            <hr />\n" +
                "            <p>Username : " + email + "</p>\n" +
                "            <p>Password : " + password + "</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "\n" +
                "      <! Banner Row -->\n" +
                "      <table\n" +
                "        role=\"presentation\"\n" +
                "        bgcolor=\"#EAF0F6\"\n" +
                "        width=\"100%\"\n" +
                "        style=\"margin-top: 50px\"\n" +
                "      >\n" +
                "        <tr>\n" +
                "          <td align=\"center\" style=\"padding: 30px 30px\">\n" +
                "            <h2>Don't have the app ?</h2>\n" +
                "            <p>\n" +
                "              Currently, the driver app is only available for Android\n" +
                "              smartphones. If you have an Android smartphone, click the link\n" +
                "              below to get the app from the Google Play store.\n" +
                "            </p>\n" +
                "            <a href=\"#\"> Download</a>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>\n";

    }

    // Get the current time and date when user creates an alert
    public static String getCurrentTimeDate() {
        // dd/MMMM/yyyy HH:mm:ss
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    // Get weekday based on the supplied date
    public static String getDayFromDate(Date date) {
        Locale locale = Locale.ENGLISH;
        DateFormat formatter = new SimpleDateFormat("EEEE", locale);
        return formatter.format(date);
    }

    public static String getDate(Date date) {
        // dd/MMMM/yyyy HH:mm:ss
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        return formatter.format(date);
    }

    public static Date convertLocalDate(LocalDate date) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = date.atStartOfDay(zoneId);
        Date convertedDate = Date.from(zonedDateTime.toInstant());
        return convertedDate;
    }

    public static HashMap<String, Integer> map() {
        HashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("MON", 0);
        map.put("TUE", 0);
        map.put("WED", 0);
        map.put("THUR", 0);
        map.put("FRI", 0);
        map.put("SAT", 0);
        map.put("SUN", 0);
        return map;
    }


    public static HashMap<String, Integer> retrieveWeeklyTrips(List<Trip> trips) throws ParseException {
        HashMap<String, Integer> weeklyTrips = Helper.map();

        LocalDate current = LocalDate.now();
        LocalDate monday = current.with(previousOrSame(MONDAY));
        LocalDate tuesday = current.with(previousOrSame(TUESDAY));
        LocalDate wednesday = current.with(previousOrSame(WEDNESDAY));
        LocalDate thursday = current.with(previousOrSame(THURSDAY));
        LocalDate friday = current.with(previousOrSame(FRIDAY));
        LocalDate saturday = current.with(nextOrSame(SATURDAY));
        LocalDate sunday = current.with(nextOrSame(SUNDAY));
        Date mondayDate = Helper.convertLocalDate(monday);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(mondayDate);
        Integer dayMon = calendar1.get(Calendar.DAY_OF_WEEK);
        Date tuesdayDate = Helper.convertLocalDate(tuesday);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(tuesdayDate);
        Integer dayTue = calendar2.get(Calendar.DAY_OF_WEEK);
        Date wednesdayDate = Helper.convertLocalDate(wednesday);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(wednesdayDate);
        Integer dayWed = calendar3.get(Calendar.DAY_OF_WEEK);
        Date thursdayDate = Helper.convertLocalDate(thursday);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(thursdayDate);
        Integer dayThur = calendar3.get(Calendar.DAY_OF_WEEK);
        Date fridayDate = Helper.convertLocalDate(friday);
        Calendar calendar5 = Calendar.getInstance();
        calendar5.setTime(fridayDate);
        Integer dayFri = calendar5.get(Calendar.DAY_OF_WEEK);
        Date saturdayDate = Helper.convertLocalDate(saturday);
        Calendar calendar6 = Calendar.getInstance();
        calendar6.setTime(saturdayDate);
        Integer daySat = calendar6.get(Calendar.DAY_OF_WEEK);
        Date sundayDate = Helper.convertLocalDate(sunday);
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(sundayDate);
        Integer daySun = calendar7.get(Calendar.DAY_OF_WEEK);

        for (Trip t : trips) {
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(t.getDateCreated());

            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            // Detemine the day of the current trip
            Integer day = cursor.get(Calendar.DAY_OF_WEEK);

            if (day == dayMon) {
                weeklyTrips.put("MON", weeklyTrips.getOrDefault("MON", 0) + 1);
            } else if (day == dayTue) {
                weeklyTrips.put("TUE", weeklyTrips.getOrDefault("TUE", 0) + 1);
            } else if (day == dayWed) {
                weeklyTrips.put("WED", weeklyTrips.getOrDefault("WED", 0) + 1);
            } else if (day == dayThur) {
                weeklyTrips.put("THUR", weeklyTrips.getOrDefault("THUR", 0) + 1);
            } else if (day == dayFri) {
                weeklyTrips.put("FRI", weeklyTrips.getOrDefault("FRI", 0) + 1);
            } else if (day == daySat) {
                weeklyTrips.put("SAT", weeklyTrips.getOrDefault("SAT", 0) + 1);
            } else if (day == daySun) {
                weeklyTrips.put("SUN", weeklyTrips.getOrDefault("SUN", 0) + 1);
            }

        }

        return weeklyTrips;
    }

    public static HashMap<String, Integer> retrieveWeeklyEmergencies(List<Emergency> emergencies) throws ParseException {
        HashMap<String, Integer> weeklyEmergencies = Helper.map();

        LocalDate current = LocalDate.now();
        LocalDate monday = current.with(previousOrSame(MONDAY));
        LocalDate tuesday = current.with(previousOrSame(TUESDAY));
        LocalDate wednesday = current.with(previousOrSame(WEDNESDAY));
        LocalDate thursday = current.with(previousOrSame(THURSDAY));
        LocalDate friday = current.with(previousOrSame(FRIDAY));
        LocalDate saturday = current.with(nextOrSame(SATURDAY));
        LocalDate sunday = current.with(nextOrSame(SUNDAY));
        Date mondayDate = Helper.convertLocalDate(monday);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(mondayDate);
        Integer dayMon = calendar1.get(Calendar.DAY_OF_WEEK);
        Date tuesdayDate = Helper.convertLocalDate(tuesday);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(tuesdayDate);
        Integer dayTue = calendar2.get(Calendar.DAY_OF_WEEK);
        Date wednesdayDate = Helper.convertLocalDate(wednesday);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(wednesdayDate);
        Integer dayWed = calendar3.get(Calendar.DAY_OF_WEEK);
        Date thursdayDate = Helper.convertLocalDate(thursday);
        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(thursdayDate);
        Integer dayThur = calendar3.get(Calendar.DAY_OF_WEEK);
        Date fridayDate = Helper.convertLocalDate(friday);
        Calendar calendar5 = Calendar.getInstance();
        calendar5.setTime(fridayDate);
        Integer dayFri = calendar5.get(Calendar.DAY_OF_WEEK);
        Date saturdayDate = Helper.convertLocalDate(saturday);
        Calendar calendar6 = Calendar.getInstance();
        calendar6.setTime(saturdayDate);
        Integer daySat = calendar6.get(Calendar.DAY_OF_WEEK);
        Date sundayDate = Helper.convertLocalDate(sunday);
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(sundayDate);
        Integer daySun = calendar7.get(Calendar.DAY_OF_WEEK);


        for (Emergency e : emergencies) {
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(e.getDateCreated());

            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            // Detemine the day of the current trip
            Integer day = cursor.get(Calendar.DAY_OF_WEEK);

            if (day == dayMon) {
                weeklyEmergencies.put("MON", weeklyEmergencies.getOrDefault("MON", 0) + 1);
            } else if (day == dayTue) {
                weeklyEmergencies.put("TUE", weeklyEmergencies.getOrDefault("TUE", 0) + 1);
            } else if (day == dayWed) {
                weeklyEmergencies.put("WED", weeklyEmergencies.getOrDefault("WED", 0) + 1);
            } else if (day == dayThur) {
                weeklyEmergencies.put("THUR", weeklyEmergencies.getOrDefault("THUR", 0) + 1);
            } else if (day == dayFri) {
                weeklyEmergencies.put("FRI", weeklyEmergencies.getOrDefault("FRI", 0) + 1);
            } else if (day == daySat) {
                weeklyEmergencies.put("SAT", weeklyEmergencies.getOrDefault("SAT", 0) + 1);
            } else if (day == daySun) {
                weeklyEmergencies.put("SUN", weeklyEmergencies.getOrDefault("SUN", 0) + 1);
            }

        }

        return weeklyEmergencies;
    }


    public static HashMap<String, Integer> retrieveMonthlyTrips(List<Trip> trips) throws ParseException {
        HashMap<String, Integer> monthlyEmergencies = populateMonthMap();
        for (Trip t : trips) {
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(t.getDateCreated());
            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            Integer month = cursor.get(Calendar.MONTH);

            if (month == 0) {
                monthlyEmergencies.put("JAN", monthlyEmergencies.getOrDefault("JAN", 0) + 1);
            } else if (month == 1) {
                monthlyEmergencies.put("FEB", monthlyEmergencies.getOrDefault("FEB", 0) + 1);
            } else if (month == 2) {
                monthlyEmergencies.put("MAR", monthlyEmergencies.getOrDefault("MAR", 0) + 1);
            } else if (month == 3) {
                monthlyEmergencies.put("APR", monthlyEmergencies.getOrDefault("APR", 0) + 1);
            } else if (month == 4) {
                monthlyEmergencies.put("MAY", monthlyEmergencies.getOrDefault("MAY", 0) + 1);
            } else if (month == 5) {
                monthlyEmergencies.put("JUN", monthlyEmergencies.getOrDefault("JUN", 0) + 1);
            } else if (month == 6) {
                monthlyEmergencies.put("JUL", monthlyEmergencies.getOrDefault("JUL", 0) + 1);
            } else if (month == 7) {
                monthlyEmergencies.put("AUG", monthlyEmergencies.getOrDefault("AUG", 0) + 1);
            } else if (month == 8) {
                monthlyEmergencies.put("SEP", monthlyEmergencies.getOrDefault("SEP", 0) + 1);
            } else if (month == 9) {
                monthlyEmergencies.put("OCT", monthlyEmergencies.getOrDefault("OCT", 0) + 1);
            } else if (month == 10) {
                monthlyEmergencies.put("NOV", monthlyEmergencies.getOrDefault("NOV", 0) + 1);
            } else if (month == 11) {
                monthlyEmergencies.put("DEC", monthlyEmergencies.getOrDefault("DEC", 0) + 1);
            }

        }

        return monthlyEmergencies;
    }


    public static HashMap<String, Integer> retrieveMonthlyEmergencies(List<Emergency> emergencies) throws ParseException {
        HashMap<String, Integer> monthlyEmergencies = populateMonthMap();
        for (Emergency e : emergencies) {
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(e.getDateCreated());
            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            Integer month = cursor.get(Calendar.MONTH);

            if (month == 0) {
                monthlyEmergencies.put("JAN", monthlyEmergencies.getOrDefault("JAN", 0) + 1);
            } else if (month == 1) {
                monthlyEmergencies.put("FEB", monthlyEmergencies.getOrDefault("FEB", 0) + 1);
            } else if (month == 2) {
                monthlyEmergencies.put("MAR", monthlyEmergencies.getOrDefault("MAR", 0) + 1);
            } else if (month == 3) {
                monthlyEmergencies.put("APR", monthlyEmergencies.getOrDefault("APR", 0) + 1);
            } else if (month == 4) {
                monthlyEmergencies.put("MAY", monthlyEmergencies.getOrDefault("MAY", 0) + 1);
            } else if (month == 5) {
                monthlyEmergencies.put("JUN", monthlyEmergencies.getOrDefault("JUN", 0) + 1);
            } else if (month == 6) {
                monthlyEmergencies.put("JUL", monthlyEmergencies.getOrDefault("JUL", 0) + 1);
            } else if (month == 7) {
                monthlyEmergencies.put("AUG", monthlyEmergencies.getOrDefault("AUG", 0) + 1);
            } else if (month == 8) {
                monthlyEmergencies.put("SEP", monthlyEmergencies.getOrDefault("SEP", 0) + 1);
            } else if (month == 9) {
                monthlyEmergencies.put("OCT", monthlyEmergencies.getOrDefault("OCT", 0) + 1);
            } else if (month == 10) {
                monthlyEmergencies.put("NOV", monthlyEmergencies.getOrDefault("NOV", 0) + 1);
            } else if (month == 11) {
                monthlyEmergencies.put("DEC", monthlyEmergencies.getOrDefault("DEC", 0) + 1);
            }

        }

        return monthlyEmergencies;
    }


    public static HashMap<String, Integer> topEmergencyCategories() {

        return null;
    }

    public static HashMap<String, Integer> populateMonthMap() {
        HashMap<String, Integer> monthlyEmergencies = new LinkedHashMap<>();
        monthlyEmergencies.put("JAN", 0);
        monthlyEmergencies.put("FEB", 0);
        monthlyEmergencies.put("MAR", 0);
        monthlyEmergencies.put("APR", 0);
        monthlyEmergencies.put("MAY", 0);
        monthlyEmergencies.put("JUN", 0);
        monthlyEmergencies.put("JUL", 0);
        monthlyEmergencies.put("AUG", 0);
        monthlyEmergencies.put("SEP", 0);
        monthlyEmergencies.put("OCT", 0);
        monthlyEmergencies.put("NOV", 0);
        monthlyEmergencies.put("DEC", 0);
        return monthlyEmergencies;
    }

    // given List should Return A Random Element
    public static Driver getRandomElement(List<Driver> drivers) {
        Arrays.asList(drivers);
        Random random = new Random();
        return drivers.get(random.nextInt(drivers.size()));
    }

    public static int randomNumber() {
        Random random = new Random();
        return random.nextInt(5);
    }

    public static Driver generateDriver(DriverRequest driver) {
        return new Driver(driver.getTitle(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getGender(),
                driver.getDateCreated(),
                driver.getOrganisation(),
                driver.getUser(),
                driver.getAdminId());
    }

    public static Student generateStudent(StudentRequest request) {
        return new Student(
                request.getUsername(),
                request.getOrganisation(),
                request.getUser()
        );
    }

    // The following method will allow to avoid number format exception when converting the comma separated string
    public static double convertStringToDecimal(String location) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        ParsePosition parsePosition = new ParsePosition(0);
        Number number = numberFormat.parse(location, parsePosition);
        if (parsePosition.getIndex() != location.length()) {
            throw new ParseException("Location invalid", parsePosition.getIndex());
        }
        return number.doubleValue();
    }

    public void uploadFiles(List<MultipartFile> files, String uniqueAlertId) {
        for (MultipartFile file : files) {
            try {
                imageStorageService.save(file, uniqueAlertId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void uploadFiles2(MultipartFile file, Long driverId) {
        try {
            imageStorageService.save2(file, driverId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Returns a sorted HashMap
     * The values of the HashMap will be from highest to lowest
     */
    public static LinkedHashMap<String, Float> sortMap(Map<String, Float> map) {
        LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<>();
        ArrayList<Float> arrayList = new ArrayList<>();
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            arrayList.add(entry.getValue());
        }
        Collections.sort(arrayList);
        for (Float i : arrayList) {
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                if (entry.getValue().equals(i)) {
                    sortedMap.put(entry.getKey(), i);
                }
            }
        }
        return sortedMap;

    }

    /*
     * Remove whitespaces from string and return 3 letters
     */
    public static String removeWhiteSpaces(String word) {
        return word.replaceAll("\\B.|\\P{L}", "").toUpperCase();
    }


    /*
     * Extracts an email domain
     * Returns the string after @ sign
     */
    public static String extractDomain(String email) {
        return StringUtils.substringAfter(email, "@");
    }

    // Messages in case of errors or certain conditions are met
    public final static String EMAIL_MSG = "Hi %1$s\n\nYour driver account has been activated, please use the following " +
            "credentials on the mobile app:\nUsername/Email: %2$s\nPassword: %3$s";
    public final static String REGISTRATION_EMAIL_EXISTS = "Email [%s] already in use";
    public static final String NOT_FOUND = "Organisation with %s doesn't exist";
    public static final String USER_NOT_FOUND = "User with id %s doesn't exist";
    public static final String ORG_NOT_FOUND = "Organisation with id %s doesn't exist";

    //private static final String ORG_NOT_FOUND = "Organisation with id %s doesn't exist";
    public final static String REGISTRATION_SUCCESS = "Account for [%s] registered successfully";
    //private final static String REGISTRATION_EMAIL_EXISTS = "Email [%s] already in use";
    public static final String REGISTRATION_ORG_ERROR = "Your organisation is not yet available";
    public static final String ACCOUNT_INFO = "ACTIVE USER:" +
            "\nID: %1$s\nEMAIL: %2$s\nROLES: %3$s";
    public static final String ALERT_CREATED = "Alert posted {\n%s\n}";

    public static final String DRIVER_ID_NOT_FOUND = "Driver id %s does not exist";
}