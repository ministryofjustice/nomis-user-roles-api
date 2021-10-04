CREATE OR REPLACE PACKAGE OMS_OWNER.OMS_UTILS
IS
   TYPE varchar2_array_table_type IS TABLE OF VARCHAR2 (240)
      INDEX BY BINARY_INTEGER;

   TYPE g_staff_rec IS RECORD (
      staff_id      staff_members.staff_id%TYPE,
      last_name     staff_members.last_name%TYPE,
      first_name    staff_members.first_name%TYPE,
      middle_name   staff_members.last_name%TYPE
   );

   TYPE g_offender_booking_rec IS RECORD (
      offender_id           offenders.offender_id%TYPE,
      offender_id_display   offenders.offender_id_display%TYPE,
      last_name             offenders.last_name%TYPE,
      first_name            offenders.first_name%TYPE,
      middle_name           offenders.last_name%TYPE,
      root_offender_id      offender_bookings.root_offender_id%TYPE
   );

   search_string_c   CONSTANT VARCHAR2 (2) DEFAULT '^s';

   FUNCTION show_version
      RETURN VARCHAR2;

   FUNCTION get_staff_id
      RETURN NUMBER;

   FUNCTION get_staff_name (p_staff_id staff_members.staff_id%TYPE)
      RETURN VARCHAR2;

   FUNCTION get_staff_name_rec (p_staff_id staff_members.staff_id%TYPE)
      RETURN g_staff_rec;

   FUNCTION combine_date_time (p_date DATE, p_time DATE)
      RETURN DATE;

   PROCEDURE get_staff_id_and_name (
      p_user_id      IN       VARCHAR2 DEFAULT USER,
      p_staff_id     OUT      staff_members.staff_id%TYPE,
      p_staff_name   OUT      VARCHAR2
   );

   --
   -- To check if movement reason is valid.
   FUNCTION check_mov_reas (
      p_movement_type          IN   VARCHAR2,
      p_movement_reason_code   IN   VARCHAR2
   )
      RETURN NUMBER;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number       IN   NUMBER,
      p_application_system   IN   VARCHAR2
   )
      RETURN VARCHAR2;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number          IN   NUMBER,
      p_application_system      IN   VARCHAR2,
      p_message_parameter_one   IN   VARCHAR2
   )
      RETURN VARCHAR2;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number          IN   NUMBER,
      p_application_system      IN   VARCHAR2,
      p_message_parameter_one   IN   VARCHAR2,
      p_message_parameter_two   IN   VARCHAR2
   )
      RETURN VARCHAR2;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number            IN   NUMBER,
      p_application_system        IN   VARCHAR2,
      p_message_parameter_one     IN   VARCHAR2,
      p_message_parameter_two     IN   VARCHAR2,
      p_message_parameter_three        VARCHAR2
   )
      RETURN VARCHAR2;

   --
   -- REVOKE A ROLE FROM A USER
   PROCEDURE revoke_role (p_role_name VARCHAR2, p_user_name VARCHAR2);

   --
   -- GRANT A ROLE TO A USER
   PROCEDURE grant_role (p_role_name VARCHAR2, p_user_name VARCHAR2);

   --
   -- CHANGE A USER'S PASSWORD
   PROCEDURE change_user_password (p_user_name VARCHAR2, p_password VARCHAR2);

   --
   -- To return the termination date of a staff member.
   FUNCTION get_termination_date (p_staff_id IN NUMBER)
      RETURN DATE;

   -- CREATE AN ORACLE USER
   PROCEDURE create_user (
      p_user_name   VARCHAR2,
      p_password    VARCHAR2,
      p_profile     VARCHAR2 DEFAULT 'tag_general'
   );

   PROCEDURE change_user_profile (
      p_user_name   VARCHAR2,
      p_profile     VARCHAR2 DEFAULT 'tag_general'
   );

   --
   -- DROP AN ORACLE USER
   PROCEDURE drop_user (p_user_name VARCHAR2);

   PROCEDURE lock_user (p_user_name VARCHAR2);

   PROCEDURE unlock_user (p_user_name VARCHAR2);

   PROCEDURE expire_password (p_user_name VARCHAR2);

   PROCEDURE update_personnel_card_status;

   PROCEDURE drop_terminated_users;

   FUNCTION deactivate_location (p_agy_loc_id VARCHAR2)
      RETURN BOOLEAN;

   --
   -- Check if the staff member has the same agency location/position/role c
   FUNCTION to_date_null (
      p_cal_agy_loc_id   VARCHAR2,
      p_sac_staff_id     NUMBER,
      p_from_date        DATE,
      p_position         VARCHAR2,
      p_role             VARCHAR2
   )
      RETURN BOOLEAN;

   --
   -- FROM date check on table STAFF_LOCATION_ROLES
   FUNCTION get_to_date (
      p_cal_caseload_id   VARCHAR2,
      p_cal_agy_loc_id    VARCHAR2,
      p_sac_caseload_id   VARCHAR2,
      p_sac_staff_id      NUMBER,
      p_from_date         DATE,
      p_position          VARCHAR2,
      p_role              VARCHAR2
   )
      RETURN DATE;

   --
   -- Check that staff with the same name (last,first) and birthdate exist
   FUNCTION duplicate_member (
      p_last_name    VARCHAR2,
      p_first_name   VARCHAR2,
      p_birthdate    VARCHAR2
   )
      RETURN BOOLEAN;

   --
   -- To validate a password for OMS.
   FUNCTION validate_password (
      p_password         IN       VARCHAR2,
      p_min_passwd_len   OUT      NUMBER
   )
      RETURN NUMBER;

   --
   -- To check that booking dates do not overlap with any other booking.
   FUNCTION check_booking_dates (
      p_offender_id_display   IN   VARCHAR2,
      p_offender_book_id      IN   NUMBER,
      p_booking_begin_date    IN   DATE,
      p_booking_end_date      IN   DATE
   )
      RETURN BOOLEAN;

   --
   -- To check if a property caseload is valid.
   FUNCTION check_property_caseload (p_caseload_id IN VARCHAR2)
      RETURN NUMBER;

   -- Gets the last booking previous to p_last_book_no
   FUNCTION get_previous_booking_no (
      p_offender_id_display        VARCHAR2,
      p_book_no               IN   VARCHAR2,
      p_booking_type          IN   VARCHAR2
   )
      RETURN VARCHAR2;

   PROCEDURE change_role_password (
      p_role_name   IN   VARCHAR2,
      p_password    IN   VARCHAR2
   );

   FUNCTION get_offender_booking_rec (
      p_offender_book_id   IN   offender_bookings.offender_book_id%TYPE
   )
      RETURN g_offender_booking_rec;

   FUNCTION get_profile (p_user VARCHAR2)
      RETURN VARCHAR2;

   PROCEDURE get_password_attempts (
      p_no     OUT   VARCHAR2,
      p_time   OUT   VARCHAR2,
      p_user         VARCHAR2
   );

   FUNCTION get_username(usrname VARCHAR2)
      RETURN VARCHAR2;

PROCEDURE get_staff_member_dtls (
 p_username         IN      STAFF_USER_ACCOUNTS.username%TYPE,
 p_last_name        OUT     STAFF_MEMBERS.last_name%TYPE,
 p_first_name       OUT     STAFF_MEMBERS.first_name%TYPE,
 p_middle_name      OUT     STAFF_MEMBERS.middle_name%TYPE,
 p_status           OUT     STAFF_MEMBERS.status%TYPE,
 p_staff_id         OUT     STAFF_MEMBERS.staff_id%TYPE
 );


PROCEDURE delete_stakeholder_account (p_stakeholder_id IN stakeholder_accounts.stakeholder_id%TYPE);


END oms_utils;
/

CREATE OR REPLACE PACKAGE BODY OMS_OWNER.OMS_UTILS
IS
-- =====================================================================================
   vcp_version   CONSTANT VARCHAR2(20) := '5.3  17-Sep-2021';
-- =====================================================================================
/* MODIFICATION HISTORY
  Version     Developer       Date        Comments
  ---------   -----------     ----------  ------------------------------------------
  5.3         Claus           17-Sep-2021 SDU-758. Changed delete_stakeholder_account. Delete user_stakeholder_caseloads before deleting stakeholder_caseloads.
  5.2         Claus           29-Mar-2021 SDU-719. Delete from user_stakeholder_caseloads when deleting stakeholder account.
  5.1         Claus           07-Dec-2020 SDU-694. Added get_staff_member_dtls and delete_stakeholder_account.
  3.2         PThakur         16-Sep-2010 D#18021: Default role for users should be TAG_RO.
                                          CREATE_USER procedure has been amended.
  3.1         PThakur         19-Aug-2010 11g changes. CREATE_USER procedure has been amended.
  2.34        GJC		      11-Dec-2008 Defect 11246 Allow passwords with leading numeric
  2.33        Surya           25-Mar-2008 TD 8065:Added ORA-01740 error code in CREATE_USER procedure
  			  				  			  as it was showing error in OCUERROR for single double quotes.
  2.32        Surya	      29-Feb-2008 TD8065:Removed double quotes in the following modules as per
  			  				  			  Aleks Suggestions:
  			  				  			  1.Drop_User
										  2.Grant_Role
										  3.change_user_password
										  4.Lock_User
										  5.change_user_profile
										  6.Unlock_User
										  7.expire_password
										  8.change_role_password
										  9.revoke_role
  2.31        P Thakur        06-Feb-2008 TD8065:Modified create_user. Error code correction.
  2.30        Surya           06-Feb-2008 TD8065:Modified create_user.
  2.29        Krishna         02-Oct-2007 #7626: nothing changed, just checked out and checked into PVCS
  2.28        P PATIL	      06-AUG-2007 #7626 lname  modified to VARCHAR2(75)
  2.27        P PATIL	      03-AUG-2007 #7626 Added - FUNCTION get_username for CN02 report.
  2.26        Surya			  25-Jul-2007 User Admin Security - TD7428:Added get_profile and get_password_attempts.
  2.25        Surya           12-Jul-2007 User Admin Security - Modified get_staff_id_and_name sub-routine.
  2.24        Surya           09-Jul-2007 User Admin Security - Retrofit as per new security:
  			  				  			  Modified the following:
										  1.add_accessible_caseloads - Removed
										  2.get_termination_date
										  3.drop_terminated_users
										  4.delete_user_groups - Removed.
										  5.check_role_access - Removed.
										  6.chk_users_exist - Removed
  2.23        Surya           08-Jul-2007 User Admin Security - User Id impacted objects fix.
                                          Modified get_staff_id and get_staff_id_and_name.
  2.22        GJC             25-May-2007 Add CHANGE_USER_PROFILE
  2.21        GJC             21-May-2007 Double quote usernames
  2.20        GJC             15-May-2007 Add LOCK_USER and UNLOCK_USER
  2.19        GJC             11-May-2007 Add profile to create user ddl
  2.18        Prashant P      29-Mar-2007 #6355 - Inserted a space after Last_name
  2.17        GJC             14-Oct-2006 Remove DBMS_OUTPUT calls
  2.16        Surya           24-Apr-2006 Re-Created the Create_User procedure with the dynamic
   			  				  			  native sql statements.
  2.15        Surya           24-Apr-2006 Modified the Create_User Procedure, oracle error
  			  				  			  was coming while log into the application for the new
										  user:
  			  				  			  1.Removed the admin option to Tag_User Role.
										  2.Assigned the CONNECT Role to New User
										  3.Made the Tag_User Role as the default role to the user.
  2.14	  D Rice	      14-Apr-2006 Added Function: get_offender_booking_rec
  2.13        Venu            03-Jan-2006 D# 47. Removed get_db_sequence_no from package body.
  2.12	  M Cox	      29-Dec-2005 Removed Grant statements from script.
  2.11        Claus           14-Dec-2005 D# 47. Removed get_db_sequence_no and generate_next_sequence.
  2.10        Surya           02-Dec-2005 Added get_staff_name_rec function.
  2.8         Surya           07-Oct-2005 1.Added get_db_sequence_no function, which returns
                                          database object sequence's nextval dynamically.
                                          2.Added overloaded generate_next_sequence -
                                          Get the next primary key column seq by passing
                                          table name(ex:OFFENDER_SENTENCE_TERMS)
                                                ,the name of primary key1 column(ex:OFFENDER_BOOK_ID)
                                                ,the name of second primary key column(ex: SENTENCE_SEQ)
                                                ,the name of third primary key column(ex: TERM_SEQ)
                                                ,the value of first primary key column(book id value)
                                                ,the value of second primary key column(sentence seq value).
  2.7         Laurence        22-Sep-2005 Removed format_staff_name().
  2.6         Surya           22-Sep-2005 Added the Function combine_datetime which combines
                                          the date and time portions and returns the date and time.
  2.5         Laurence        15-Sep-2005 Added procedure:
                                          get_staff_id_and_name().
                                          format_staff_name() - for project-wide consistency.
                                          Modified:
                                          get_staff_name() to use format_staff_name().
  2.4         Surya           13-Sep-2005 Corrected Version Label
  2.2         Surya           25-Aug-2005 Added the following generic sub-routines for the
                                          regular development usage:
                                          1.get_staff_id - Get the staff name by passing staff_id parameter.
                                          2.get_last_name - Get last_name and first name or the staff member
                                                            by passing staff_id parameter.
                                          3.generate_next_sequence - Get the next primary key column seq
                                                            by passing table name(ex:OFFENDER_SENTENCES)
                                                            ,the name of primary key column(ex:OFFENDER_BOOK_ID)
                                                            and name of second primary key column(ex: SENTENCE_SEQ)
                                                            and the value of first primary key
                                                            column(ex: offender_book_id value - 1732).
  10.2.1      Michael         14-JUL-2005 Removed insert into Journal Table
  6.1.0.0     Vipul           08-APR-2002 Modified create_user proc to  grant create session
                                          privileges before granting tag_user.
  4.9.0.0     Simon           06/27/2000  modified CREATE_USER for Role-based security
                                          refer to comments in the procedure
  4.9.0.1     Simon           06/29/2000  new procedure CHANGE_ROLE_PASSWORD created to
                                          change the password of a ROLE.  currently called
                                          by the system profiles screen OUMSYPFL
  4.9.0.1     Simon           07/05/2000  user created are granted with the admin option
                                          so that they in turn can grant roles to the
                                          users they create
  4.9.0.2     Surya           09-AUG-2000 Added the SHOW_VERSION().
*/
/***************************************************************************************/
   FUNCTION Show_Version
   RETURN VARCHAR2
   IS
   BEGIN
      RETURN(vcp_version);
   END Show_Version;
/***************************************************************************************/
FUNCTION get_staff_id
   RETURN NUMBER
IS
   --@@@Surya 05-Jul-2007:User Admin Security fix.
   CURSOR staff_cur
   IS
      SELECT staff_id
	    FROM staff_user_accounts
	   WHERE username = USER;

   v_staff_id   STAFF_MEMBERS.staff_id%TYPE   := NULL;
BEGIN
   OPEN staff_cur;
   FETCH staff_cur INTO v_staff_id;
   CLOSE staff_cur;

   RETURN v_staff_id;
EXCEPTION
   WHEN OTHERS
   THEN
      Tag_Error.handle(Tag_Error.c_programs_err);
END get_staff_id;                                                   --Function
/***************************************************************************************/
FUNCTION get_staff_name (p_staff_id STAFF_MEMBERS.staff_id%TYPE)
   RETURN VARCHAR2
IS
   CURSOR staff_cur
   IS
      SELECT last_name || ', ' || first_name -- #6355 - Inserted a space after Last_name,
        FROM STAFF_MEMBERS
       WHERE staff_id = p_staff_id;

   v_name   VARCHAR2 (75);
BEGIN
   OPEN staff_cur;
   FETCH staff_cur INTO v_name;
   CLOSE staff_cur;

   RETURN v_name;
EXCEPTION
   WHEN OTHERS
   THEN
      Tag_Error.handle(Tag_Error.c_programs_err);
END get_staff_name;                                                 --Function
/***************************************************************************************/
FUNCTION get_staff_name_rec (p_staff_id STAFF_MEMBERS.staff_id%TYPE)
   RETURN g_staff_rec
IS
   CURSOR staff_cur
   IS
      SELECT last_name,
             first_name,
             middle_name
        FROM STAFF_MEMBERS
       WHERE staff_id = p_staff_id;

   v_staff_rec   g_staff_rec;
BEGIN
   OPEN staff_cur;

   FETCH staff_cur
    INTO v_staff_rec.last_name,
         v_staff_rec.first_name,
         v_staff_rec.middle_name;

   CLOSE staff_cur;

   RETURN v_staff_rec;
EXCEPTION
   WHEN OTHERS
   THEN
      Tag_Error.handle;
END get_staff_name_rec;                                                 --Function
/***************************************************************************************/
FUNCTION Combine_Date_Time (p_date DATE, p_time DATE)
   RETURN DATE
IS
   v_return_datetime   DATE;
BEGIN
   v_return_datetime :=
      TO_DATE (TO_CHAR (TRUNC (p_date) + (p_time - TRUNC (p_time)),
                        'DD-MON-YYYY HH24:MI:SS'
                       ),
               'DD-MON-YYYY HH24:MI:SS'
              );
   RETURN v_return_datetime;
EXCEPTION
   WHEN OTHERS
   THEN
      Tag_Error.handle;
END Combine_Date_Time;
/***************************************************************************************/
PROCEDURE get_staff_id_and_name (
   p_user_id IN VARCHAR2 DEFAULT USER,
   p_staff_id OUT STAFF_MEMBERS.staff_id%TYPE,
   p_staff_name OUT VARCHAR2 )
IS
	--@@@Surya 05-Jul-2007:User Admin Security fix.Removed no_data_found exceptions.
   CURSOR staff_det_cur
   IS
      SELECT a.staff_id,
	         b.last_name || ',' || b.first_name
	    FROM staff_user_accounts a, staff_members b
	   WHERE a.username = p_user_id
	     AND a.staff_id = b.staff_id;

    l_notfound BOOLEAN;

BEGIN
   OPEN staff_det_cur;
   FETCH staff_det_cur INTO p_staff_id, p_staff_name;
   CLOSE staff_det_cur;
EXCEPTION
   WHEN OTHERS
   THEN
      Tag_Error.handle(Tag_Error.c_programs_err);
END get_staff_id_and_name;
--
-- To check if a property caseload is valid.
--
   FUNCTION check_property_caseload (
            p_caseload_id           IN VARCHAR2)
     RETURN NUMBER
   IS
   --
   -- Work Variables
   --
      l_count   NUMBER;
   BEGIN
      SELECT COUNT (*)
        INTO l_count
        FROM CASELOAD_AGENCY_LOCATIONS ca
       WHERE ca.caseload_id = p_caseload_id
         AND ca.agy_loc_id NOT IN ('OUT', 'TRN');
      RETURN (l_count);
   EXCEPTION
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR ( -20000, Oms_Utils.display_user_message (10, 'OMS', 'OMFCPCSL', SQLERRM));
   END;

--
-- To check if movement reason is valid.
--
   FUNCTION check_mov_reas (
            p_movement_type          IN   VARCHAR2,
            p_movement_reason_code   IN   VARCHAR2)
     RETURN NUMBER
   IS
   --
   -- Work Variables
   --
      l_exists   INTEGER;
   BEGIN
      SELECT 1
        INTO l_exists
        FROM dual
       WHERE EXISTS (SELECT 1
                       FROM MOVEMENT_REASONS
                      WHERE movement_type = p_movement_type
                        AND movement_reason_code = p_movement_reason_code);
      RETURN l_exists;
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN 0;
      WHEN TOO_MANY_ROWS
      THEN
         RETURN 1;
   END;
--
-- To display a user message on the screen.
--
   FUNCTION display_user_message (
            p_message_number       IN   NUMBER,
            p_application_system   IN   VARCHAR2 )
     RETURN VARCHAR2
   IS
   --
   -- A SQL cursor to get a system message
   --
      CURSOR get_system_messages_cur
      IS
         SELECT sys_msg.message_type,
                sys_msg.message_text,
                sys_msg.action_text,
                sys_msg.system_remarks_text
           FROM SYSTEM_MESSAGES sys_msg
          WHERE sys_msg.message_number = p_message_number
            AND sys_msg.appln_code = p_application_system;

      --
      --
      v_system_remarks_text   VARCHAR2(240);
      --
      --
      v_action_text           VARCHAR2(240);
      --
      --
      v_message_type          VARCHAR2(240);
      --
      --
      v_message_text          VARCHAR2(240);
   BEGIN
      -- if cursor not already open, then open it.
      IF NOT get_system_messages_cur%ISOPEN
      THEN
         OPEN get_system_messages_cur;
      END IF;

      -- get the message
      FETCH get_system_messages_cur INTO v_message_type,
                                         v_message_text,
                                         v_action_text,
                                         v_system_remarks_text;

      -- handle if message not found
      IF get_system_messages_cur%NOTFOUND
      THEN
         CLOSE get_system_messages_cur;
         RETURN ('Message number ' ||
                TO_CHAR (p_message_number) ||
                ' not found in SYSTEM_MESSAGES table. Call support');
      END IF;

      CLOSE get_system_messages_cur;
      -- return the error
      RETURN (p_application_system ||
             ' ' ||
             TO_CHAR (p_message_number) ||
             ' ' ||
             v_message_type ||
             ' ' ||
             v_message_text ||
             ' ' ||
             v_action_text);
   END;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number          IN   NUMBER,
      p_application_system      IN   VARCHAR2,
      p_message_parameter_one   IN   VARCHAR2
      )
      RETURN VARCHAR2
   IS
      --
      -- A SQL cursor to get a system message
      CURSOR get_system_messages_cur
      IS
         SELECT sys_msg.message_type,
                sys_msg.message_text,
                sys_msg.action_text,
                sys_msg.system_remarks_text
           FROM SYSTEM_MESSAGES sys_msg
          WHERE sys_msg.message_number = p_message_number
            AND sys_msg.appln_code = p_application_system;

      --
      --
      v_system_remarks_text       VARCHAR2(240);
      --
      --
      v_action_text               VARCHAR2(240);
      --
      --
      v_message_type              VARCHAR2(240);
      --
      --
      v_message_text              VARCHAR2(240);
      --
      --
      v_character_location        NUMBER;
      --
      --
      v_old_character_location    NUMBER;
      --
      --
      v_length_of_search_string   NUMBER;
      --
      --
      v_table_counter             BINARY_INTEGER;
      --
      --
      v_message_table             Oms_Utils.varchar2_array_table_type;
      --
      --
      v_message_table_length      BINARY_INTEGER;

      --
      -- To contain common source code for OMFDISP[1-N]
      FUNCTION display_user_message_stub
         RETURN VARCHAR2
      IS
      BEGIN
         -- initialize variables
         v_table_counter := 0;
         v_character_location := 1;
         v_length_of_search_string := LENGTH (search_string_c);

         -- if cursor not already open, then open it.
         IF NOT get_system_messages_cur%ISOPEN
         THEN
            OPEN get_system_messages_cur;
         END IF;

         -- get the message
         FETCH get_system_messages_cur INTO v_message_type,
                                            v_message_text,
                                            v_action_text,
                                            v_system_remarks_text;

         -- handle if message not found
         IF get_system_messages_cur%NOTFOUND
         THEN
            CLOSE get_system_messages_cur;
            RETURN (p_application_system ||
                   '-' ||
                   ' -1 ERROR Message Number ' ||
                   TO_CHAR (p_message_number) ||
                   ' ' ||
                   ' not found in SYSTEM_MESSAGES table. Call support');
         END IF;

         CLOSE get_system_messages_cur;

         WHILE v_table_counter < v_message_table_length
         LOOP
            -- find the character location of the current (always the first) substitution
            v_character_location := INSTR (v_message_text, search_string_c);

            IF v_character_location = 0
            THEN
               RETURN (p_application_system ||
                      '-' ||
                      ' -2 ERROR Message Number ' ||
                      TO_CHAR (
                         p_message_number
                      ) ||
                      ' ' ||
                      ' has been called with an invalid # of arguments. Call support');
            END IF;

            -- put the substitution in and skip the search string
            v_message_text :=
               SUBSTR (v_message_text, 1, v_character_location - 1) ||
               v_message_table (v_table_counter) ||
               SUBSTR (
                  v_message_text,
                  v_character_location + v_length_of_search_string
               );
            v_table_counter := v_table_counter + 1;
         END LOOP;

         -- display the error
         RETURN (p_application_system ||
                '-' ||
                TO_CHAR (p_message_number) ||
                ' ' ||
                v_message_type ||
                ' ' ||
                v_message_text ||
                ' ' ||
                v_action_text);
      END;
   BEGIN
      -- initialise variables
      v_message_table_length := 1;
      v_message_table (0) := p_message_parameter_one;
      RETURN display_user_message_stub;
   END;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number          IN   NUMBER,
      p_application_system      IN   VARCHAR2,
      p_message_parameter_one   IN   VARCHAR2,
      p_message_parameter_two   IN   VARCHAR2
      )
      RETURN VARCHAR2
   IS
      --
      -- A SQL cursor to get a system message
      CURSOR get_system_messages_cur
      IS
         SELECT sys_msg.message_type,
                sys_msg.message_text,
                sys_msg.action_text,
                sys_msg.system_remarks_text
           FROM SYSTEM_MESSAGES sys_msg
          WHERE sys_msg.message_number = p_message_number
            AND sys_msg.appln_code = p_application_system;

      --
      --
      v_system_remarks_text       VARCHAR2(240);
      --
      --
      v_action_text               VARCHAR2(240);
      --
      --
      v_message_type              VARCHAR2(240);
      --
      --
      v_message_text              VARCHAR2(240);
      --
      --
      v_character_location        NUMBER;
      --
      --
      v_old_character_location    NUMBER;
      --
      --
      v_length_of_search_string   NUMBER;
      --
      --
      v_table_counter             BINARY_INTEGER;
      --
      --
      v_message_table             Oms_Utils.varchar2_array_table_type;
      --
      --
      v_message_table_length      BINARY_INTEGER;

      --
      -- To contain common source code for OMFDISP[1-N]
      FUNCTION display_user_message_stub
         RETURN VARCHAR2
      IS
      BEGIN
         -- initialize variables
         v_table_counter := 0;
         v_character_location := 1;
         v_length_of_search_string := LENGTH (search_string_c);

         -- if cursor not already open, then open it.
         IF NOT get_system_messages_cur%ISOPEN
         THEN
            OPEN get_system_messages_cur;
         END IF;

         -- get the message
         FETCH get_system_messages_cur INTO v_message_type,
                                            v_message_text,
                                            v_action_text,
                                            v_system_remarks_text;

         -- handle if message not found
         IF get_system_messages_cur%NOTFOUND
         THEN
            CLOSE get_system_messages_cur;
            RETURN (p_application_system ||
                   '-' ||
                   ' -1 ERROR Message Number ' ||
                   TO_CHAR (p_message_number) ||
                   ' ' ||
                   ' not found in SYSTEM_MESSAGES table. Call support');
         END IF;

         CLOSE get_system_messages_cur;

         WHILE v_table_counter < v_message_table_length
         LOOP
            -- find the character location of the current (always the first) substitution
            v_character_location := INSTR (v_message_text, search_string_c);

            IF v_character_location = 0
            THEN
               RETURN (p_application_system ||
                      '-' ||
                      ' -2 ERROR Message Number ' ||
                      TO_CHAR (
                         p_message_number
                      ) ||
                      ' ' ||
                      ' has been called with an invalid # of arguments. Call support');
            END IF;

            -- put the substitution in and skip the search string
            v_message_text :=
               SUBSTR (v_message_text, 1, v_character_location - 1) ||
               v_message_table (v_table_counter) ||
               SUBSTR (
                  v_message_text,
                  v_character_location + v_length_of_search_string
               );
            v_table_counter := v_table_counter + 1;
         END LOOP;

         -- display the error
         RETURN (p_application_system ||
                '-' ||
                TO_CHAR (p_message_number) ||
                ' ' ||
                v_message_type ||
                ' ' ||
                v_message_text ||
                ' ' ||
                v_action_text);
      END;
   BEGIN
      -- initialise variables
      v_message_table_length := 2;
      v_message_table (0) := p_message_parameter_one;
      v_message_table (1) := p_message_parameter_two;
      RETURN display_user_message_stub;
   END;

   --
   -- To display a user message on the screen.
   FUNCTION display_user_message (
      p_message_number            IN   NUMBER,
      p_application_system        IN   VARCHAR2,
      p_message_parameter_one     IN   VARCHAR2,
      p_message_parameter_two     IN   VARCHAR2,
      p_message_parameter_three        VARCHAR2
      )
      RETURN VARCHAR2
   IS
      --
      -- A SQL cursor to get a system message
      CURSOR get_system_messages_cur
      IS
         SELECT sys_msg.message_type,
                sys_msg.message_text,
                sys_msg.action_text,
                sys_msg.system_remarks_text
           FROM SYSTEM_MESSAGES sys_msg
          WHERE sys_msg.message_number = p_message_number
            AND sys_msg.appln_code = p_application_system;

      --
      --
      v_system_remarks_text       VARCHAR2(240);
      --
      --
      v_action_text               VARCHAR2(240);
      --
      --
      v_message_type              VARCHAR2(240);
      --
      --
      v_message_text              VARCHAR2(240);
      --
      --
      v_character_location        NUMBER;
      --
      --
      v_old_character_location    NUMBER;
      --
      --
      v_length_of_search_string   NUMBER;
      --
      --
      v_table_counter             BINARY_INTEGER;
      --
      --
      v_message_table             Oms_Utils.varchar2_array_table_type;
      --
      --
      v_message_table_length      BINARY_INTEGER;

      --
      -- To contain common source code for OMFDISP[1-N]
      FUNCTION display_user_message_stub
         RETURN VARCHAR2
      IS
      BEGIN
         -- initialize variables
         v_table_counter := 0;
         v_character_location := 1;
         v_length_of_search_string := LENGTH (search_string_c);

         -- if cursor not already open, then open it.
         IF NOT get_system_messages_cur%ISOPEN
         THEN
            OPEN get_system_messages_cur;
         END IF;

         -- get the message
         FETCH get_system_messages_cur INTO v_message_type,
                                            v_message_text,
                                            v_action_text,
                                            v_system_remarks_text;

         -- handle if message not found
         IF get_system_messages_cur%NOTFOUND
         THEN
            CLOSE get_system_messages_cur;
            RETURN (p_application_system ||
                   '-' ||
                   ' -1 ERROR Message Number ' ||
                   TO_CHAR (p_message_number) ||
                   ' ' ||
                   ' not found in SYSTEM_MESSAGES table. Call support');
         END IF;

         CLOSE get_system_messages_cur;

         WHILE v_table_counter < v_message_table_length
         LOOP
            -- find the character location of the current (always the first) substitution
            v_character_location := INSTR (v_message_text, search_string_c);

            IF v_character_location = 0
            THEN
               RETURN (p_application_system ||
                      '-' ||
                      ' -2 ERROR Message Number ' ||
                      TO_CHAR (
                         p_message_number
                      ) ||
                      ' ' ||
                      ' has been called with an invalid # of arguments. Call support');
            END IF;

            -- put the substitution in and skip the search string
            v_message_text :=
               SUBSTR (v_message_text, 1, v_character_location - 1) ||
               v_message_table (v_table_counter) ||
               SUBSTR (
                  v_message_text,
                  v_character_location + v_length_of_search_string
               );
            v_table_counter := v_table_counter + 1;
         END LOOP;

         -- display the error
         RETURN (p_application_system ||
                '-' ||
                TO_CHAR (p_message_number) ||
                ' ' ||
                v_message_type ||
                ' ' ||
                v_message_text ||
                ' ' ||
                v_action_text);
      END;
   BEGIN
      -- initialise variables
      v_message_table_length := 3;
      v_message_table (0) := p_message_parameter_one;
      v_message_table (1) := p_message_parameter_two;
      v_message_table (2) := p_message_parameter_three;
      RETURN display_user_message_stub;
   END;

   --
   -- REVOKE A ROLE FROM A USER
   PROCEDURE revoke_role (p_role_name VARCHAR2, p_user_name VARCHAR2)
   IS
      role_not_granted   EXCEPTION;
      PRAGMA EXCEPTION_INIT (role_not_granted, -1951);
      revoke_cursor      INTEGER;
      l_status           INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:Td 8086:Removed double quotes
      BEGIN
         revoke_cursor := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (
            revoke_cursor,
            'REVOKE ' || p_role_name || ' FROM ' || p_user_name ,
            DBMS_SQL.v7
         );
         l_status := DBMS_SQL.EXECUTE (revoke_cursor);
      EXCEPTION
         WHEN role_not_granted
         THEN
            NULL;
      END;

      DBMS_SQL.CLOSE_CURSOR (revoke_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (revoke_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (revoke_cursor);
         END IF;

         RAISE;
   END;

   --
   -- GRANT A ROLE TO A USER
   PROCEDURE grant_role (p_role_name VARCHAR2, p_user_name VARCHAR2)
   IS
      --
      --
      grant_cursor   INTEGER;
      --
      --
      l_status       INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:Td 8086:Removed double quotes
      grant_cursor := DBMS_SQL.OPEN_CURSOR;
      DBMS_SQL.PARSE (
         grant_cursor,
         'GRANT ' || p_role_name || ' TO ' || p_user_name ,
         DBMS_SQL.v7
      );
      l_status := DBMS_SQL.EXECUTE (grant_cursor);
      DBMS_SQL.CLOSE_CURSOR (grant_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (grant_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (grant_cursor);
         END IF;

         RAISE;
   END;

   PROCEDURE password_failed (p_error_message IN VARCHAR2)
   IS
      lv_error_text VARCHAR2(1000);
   BEGIN
      lv_error_text := p_error_message;
      lv_error_text := SUBSTR(lv_error_text, INSTR(lv_error_text, 'ORA-28003') + 10, 1000);
      lv_error_text := SUBSTR(lv_error_text, INSTR(lv_error_text, 'ORA-') + 10, 1000);
      Tag_Error.raise_app_error (-20001, lv_error_text);
   END;
   --
   -- CHANGE A USER'S PASSWORD
   PROCEDURE change_user_password (p_user_name VARCHAR2, p_password VARCHAR2)
   IS
      --
      --
      change_pword_cur   INTEGER;
      --
      --
      l_status           INTEGER;
      PRAGMA AUTONOMOUS_TRANSACTION;
   BEGIN
      --@@@Surya 29-Feb-2008:Removed double quotes
      -- GJC Defect 11246 add double quotes back in
      change_pword_cur := DBMS_SQL.OPEN_CURSOR;
      DBMS_SQL.PARSE (
         change_pword_cur,
         'ALTER USER ' || p_user_name || ' IDENTIFIED BY "' || p_password ||'"',
         DBMS_SQL.v7
      );
      l_status := DBMS_SQL.EXECUTE (change_pword_cur);
      DBMS_SQL.CLOSE_CURSOR (change_pword_cur);
   EXCEPTION
   WHEN OTHERS
   THEN
      IF SQLCODE = -28003
	  THEN
	     password_failed(SQLERRM);
	  ELSIF SQLCODE = -28007--@@@Surya 24-Jul-2007 TD7428
	  THEN
	     RAISE_APPLICATION_ERROR(-20087, 'Password cannot be reused');
	  ELSE
         Tag_Error.handle;
	  END IF;
   END;

   --
   -- To return the termination date of a staff member.
   FUNCTION get_termination_date (p_staff_id IN NUMBER)
      RETURN DATE
   IS
      l_termination_date   STAFF_MEMBERS.termination_date%TYPE;
   BEGIN
      SELECT termination_date
        INTO l_termination_date
        FROM STAFF_MEMBERS
       WHERE staff_id = p_staff_id;
      RETURN (l_termination_date);
   END;

   --
   -- To check a password is valid.
   FUNCTION check_valid_password (
      p_password        IN       VARCHAR2,
      p_illegal_chars   OUT      VARCHAR2
      )
      RETURN NUMBER
   IS
      --
      --
      l_illegal_chars   VARCHAR2(32);
   BEGIN
      IF (INSTR (
            'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
            SUBSTR (UPPER (p_password), 1, 1)
         ) =
            0)
      THEN
         RETURN (-1);
      END IF;

      SELECT RTRIM (
                LTRIM (
                   TRANSLATE (
                      UPPER (p_password),
                      'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_',
                      ' '
                   ),
                   ' '
                ),
                ' '
             )
        INTO
             l_illegal_chars
        FROM dual;

      IF (l_illegal_chars IS NOT NULL)
      THEN
         p_illegal_chars := l_illegal_chars;
         RETURN (-2);
      END IF;

      RETURN (0);
   END;


/***************************************************************************************/
--@@@Surya 24-Apr-2006:Replaced the commented below procedure with the new one, as it
--                     was using dbms_sql's.
PROCEDURE create_user (
   p_user_name            VARCHAR2,
   p_password             VARCHAR2,
   p_profile              VARCHAR2 DEFAULT 'tag_general'
)
IS
      PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
   -- GJC Defect 11246 add double quotes back in
   --@@@Surya 06-Feb-2008:TD 8065-Removed double quotes as per Aleks suggestions and added exception
   --		  				      to show custom message in a Form.
   EXECUTE IMMEDIATE    'CREATE USER '
                     || p_user_name
                     || ' IDENTIFIED BY "'
                     || p_password
                     || '" PROFILE '
                     || p_profile;

   EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO ' || p_user_name ;

   EXECUTE IMMEDIATE 'GRANT TAG_USER TO ' || p_user_name;

   EXECUTE IMMEDIATE 'GRANT TAG_RO TO ' || p_user_name; -- @@@ PThakur 16/09/2010 D#18021: Added TAG_RO role for 11g

   EXECUTE IMMEDIATE 'GRANT CONNECT TO ' || p_user_name ;

   -- @@@ Venu Feb 08, 2010, When a new user is created Default Role should be set to NONE instead of TAG_USER.
   --EXECUTE IMMEDIATE 'ALTER USER ' || p_user_name || ' DEFAULT ROLE TAG_USER';

   -- @@@ PThakur 16/09/2010 D#18021: Sub-sequent change to the above mentioned change, default role should be TAG_RO.
   EXECUTE IMMEDIATE 'ALTER USER ' || p_user_name || ' DEFAULT ROLE TAG_RO';

EXCEPTION
   WHEN OTHERS
   THEN
      IF SQLCODE = -28003
	  THEN
	     password_failed(SQLERRM);
      ELSIF ABS(SQLCODE) IN (911,922,1935, 1740) -- @@@ PThakur D#8065: Error code corrected.
	  THEN
	     RAISE_APPLICATION_ERROR(-20800, 'Invalid Username');
	  ELSE
         Tag_Error.handle;
	  END IF;
END create_user;--Procedure


PROCEDURE change_user_profile (
      p_user_name            VARCHAR2,
      p_profile              VARCHAR2 DEFAULT 'tag_general'
   )
IS
      PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
   --@@@Surya 29-Feb-2008:Td 8086:Removed double quotes
   EXECUTE IMMEDIATE    'ALTER USER '
                     || p_user_name
                     || ' PROFILE '
                     || p_profile;

EXCEPTION
   WHEN OTHERS
   THEN
         Tag_Error.handle;
END;
/***************************************************************************************/
--@@@Surya 25-Jul-2007:return profile for the user.
FUNCTION get_profile(p_user VARCHAR2)
   RETURN VARCHAR2
IS
   v_profile   SYS.dba_users.PROFILE%TYPE;
BEGIN
   SELECT PROFILE
     INTO v_profile
     FROM SYS.dba_users
    WHERE username = p_user;

   RETURN (v_profile);
EXCEPTION
   WHEN OTHERS
   THEN
      tag_error.handle;
END get_profile;                                                    --Function
/***************************************************************************************/
--@@@Surya 25-Jul-2007:Show message when user reuse the same password.
PROCEDURE get_password_attempts (p_no OUT VARCHAR2, p_time OUT VARCHAR2, p_user VARCHAR2)
IS
   v_profile   SYS.dba_users.PROFILE%TYPE;

   CURSOR limit_cur (p_resource SYS.dba_profiles.resource_name%TYPE)
   IS
      SELECT LIMIT
        FROM SYS.dba_profiles
       WHERE resource_name = p_resource
	     AND PROFILE = v_profile;
BEGIN
   v_profile := get_profile(p_user);

   OPEN limit_cur ('PASSWORD_REUSE_MAX');
   FETCH limit_cur INTO p_no;
   CLOSE limit_cur;

   OPEN limit_cur ('PASSWORD_REUSE_TIME');
   FETCH limit_cur INTO p_time;
   CLOSE limit_cur;
EXCEPTION
   WHEN OTHERS
   THEN
      tag_error.handle;
END get_password_attempts;
/***************************************************************************************/
   --
   -- DROP AN ORACLE USER
   PROCEDURE drop_user (p_user_name VARCHAR2)
   IS
      user_doesnt_exist   EXCEPTION;
      PRAGMA AUTONOMOUS_TRANSACTION;
      PRAGMA EXCEPTION_INIT (user_doesnt_exist, -1918);
      drop_cursor         INTEGER;
      l_status            INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:TD 8065 - Removed double quotes
      BEGIN
         drop_cursor := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (
            drop_cursor,
            'DROP USER  ' || p_user_name,
            DBMS_SQL.v7
         );
         l_status := DBMS_SQL.EXECUTE (drop_cursor);
      EXCEPTION
         WHEN user_doesnt_exist
         THEN
            NULL;
      END;

      DBMS_SQL.CLOSE_CURSOR (drop_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (drop_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (drop_cursor);
         END IF;

         RAISE;
   END;

   PROCEDURE lock_user (p_user_name VARCHAR2)
   IS
      user_doesnt_exist   EXCEPTION;
      PRAGMA AUTONOMOUS_TRANSACTION;
      PRAGMA EXCEPTION_INIT (user_doesnt_exist, -1918);
      drop_cursor         INTEGER;
      l_status            INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:TD 8065 - Removed double quotes
      BEGIN
         drop_cursor := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (
            drop_cursor,
            'ALTER USER ' || p_user_name ||' ACCOUNT LOCK',
            DBMS_SQL.v7
         );
         l_status := DBMS_SQL.EXECUTE (drop_cursor);
      EXCEPTION
         WHEN user_doesnt_exist
         THEN
            NULL;
      END;

      DBMS_SQL.CLOSE_CURSOR (drop_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (drop_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (drop_cursor);
         END IF;

         RAISE;
   END;

   PROCEDURE unlock_user (p_user_name VARCHAR2)
   IS
      user_doesnt_exist   EXCEPTION;
      PRAGMA AUTONOMOUS_TRANSACTION;
      PRAGMA EXCEPTION_INIT (user_doesnt_exist, -1918);
      drop_cursor         INTEGER;
      l_status            INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:TD 8065 - Removed double quotes
      BEGIN
         drop_cursor := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (
            drop_cursor,
            'ALTER USER ' || p_user_name ||' ACCOUNT UNLOCK',
            DBMS_SQL.v7
         );
         l_status := DBMS_SQL.EXECUTE (drop_cursor);
      EXCEPTION
         WHEN user_doesnt_exist
         THEN
            NULL;
      END;

      DBMS_SQL.CLOSE_CURSOR (drop_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (drop_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (drop_cursor);
         END IF;

         RAISE;
   END;

   PROCEDURE expire_password (p_user_name VARCHAR2)
   IS
      user_doesnt_exist   EXCEPTION;
      PRAGMA AUTONOMOUS_TRANSACTION;
      PRAGMA EXCEPTION_INIT (user_doesnt_exist, -1918);
      drop_cursor         INTEGER;
      l_status            INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:TD 8065 - Removed double quotes
      BEGIN
         drop_cursor := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (
            drop_cursor,
            'ALTER USER ' || p_user_name ||' password expire',
            DBMS_SQL.v7
         );
         l_status := DBMS_SQL.EXECUTE (drop_cursor);
      EXCEPTION
         WHEN user_doesnt_exist
         THEN
            NULL;
      END;

      DBMS_SQL.CLOSE_CURSOR (drop_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF (DBMS_SQL.IS_OPEN (drop_cursor))
         THEN
            DBMS_SQL.CLOSE_CURSOR (drop_cursor);
         END IF;

         RAISE;
   END;


   -- @@@ Joe Wong, 16-JAN-98.
   PROCEDURE update_personnel_card_status
   IS
   BEGIN
      UPDATE PERSONNEL_ISSUED_CARDS
         SET status = 'I',
             reason = 'EXP'
       WHERE TRUNC (expiry_date, 'DD') <= TRUNC (SYSDATE, 'DD');
   EXCEPTION
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (
               11,
               'UPDATE_PERSONNEL_CARD_STATUS',
               SQLERRM
            )
         );
   END;

   PROCEDURE drop_terminated_users
   IS
      CURSOR terminate_user_c
	  IS
	     SELECT username
		   FROM v_tag_dba_users
		  WHERE TRUNC (lock_date, 'DD') = TRUNC (SYSDATE, 'DD')	;
   BEGIN
      FOR terminate_user IN terminate_user_c
      LOOP
         BEGIN
            Oms_Utils.drop_user (terminate_user.username);
         EXCEPTION
            WHEN OTHERS
            THEN
               RAISE;
         END;
      END LOOP;
   EXCEPTION
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (
               11,
               'DROP_TERMINATED_USERS',
               SQLERRM
            )
         );
   END;

   -- Checks if active Staff Members are at an Agency Location
   FUNCTION deactivate_location (p_agy_loc_id VARCHAR2)
      RETURN BOOLEAN
   IS
      --
      --
      lv_temp   CHAR(1);
   BEGIN
      SELECT 'x'
        INTO lv_temp
        FROM STAFF_LOCATION_ROLES slr
       WHERE slr.cal_agy_loc_id = p_agy_loc_id
         AND slr.TO_DATE IS NULL;
      RETURN (FALSE);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN (TRUE);
      WHEN TOO_MANY_ROWS
      THEN
         RETURN (FALSE);
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (10, 'OMS', 'OMFDEACT', SQLERRM)
         );
   END;

   --
   -- Check if the staff member has the same agency location/position/role c
   FUNCTION to_date_null (
      p_cal_agy_loc_id   VARCHAR2,
      p_sac_staff_id     NUMBER,
      p_from_date        DATE,
      p_position         VARCHAR2,
      p_role             VARCHAR2
      )
      RETURN BOOLEAN
   IS
      --
      --
      l_temp   VARCHAR2(5);
   BEGIN
      SELECT 'YES'
        INTO l_temp
        FROM STAFF_LOCATION_ROLES
       WHERE cal_agy_loc_id = p_cal_agy_loc_id
         AND sac_staff_id = p_sac_staff_id
         AND position = p_position
         AND ROLE = p_role
         AND TO_DATE IS NULL;
      RETURN (TRUE);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN (FALSE);
   END;

   --
   -- FROM date check on table STAFF_LOCATION_ROLES
   FUNCTION get_to_date (
      p_cal_caseload_id   VARCHAR2,
      p_cal_agy_loc_id    VARCHAR2,
      p_sac_caseload_id   VARCHAR2,
      p_sac_staff_id      NUMBER,
      p_from_date         DATE,
      p_position          VARCHAR2,
      p_role              VARCHAR2
      )
      RETURN DATE
   IS
      --
      --
      l_temp   DATE;
   BEGIN
      SELECT MAX (TO_DATE)
        INTO l_temp
        FROM STAFF_LOCATION_ROLES
       WHERE cal_agy_loc_id = p_cal_agy_loc_id
         AND sac_staff_id = p_sac_staff_id
         AND position = p_position
         AND ROLE = p_role;
      RETURN l_temp;
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN NULL;
   END;
   --
   -- Check that staff with the same name (last,first) and birthdate exist
   FUNCTION duplicate_member (
      p_last_name    VARCHAR2,
      p_first_name   VARCHAR2,
      p_birthdate    VARCHAR2
      )
      RETURN BOOLEAN
   IS
      --
      --
      l_temp   VARCHAR2(1);
   BEGIN
      IF p_birthdate IS NULL
      THEN
         SELECT 'X'
           INTO l_temp
           FROM dual
          WHERE EXISTS (SELECT last_name
                          FROM STAFF_MEMBERS
                         WHERE last_name = p_last_name
                           AND first_name = p_first_name
                           AND birthdate IS NULL);
      ELSE
         SELECT 'X'
           INTO l_temp
           FROM dual
          WHERE EXISTS (SELECT last_name
                          FROM STAFF_MEMBERS
                         WHERE last_name = p_last_name
                           AND first_name = p_first_name
                           AND birthdate =
                                  TO_DATE (p_birthdate, 'dd-mon-yyyy'));
      END IF;

      IF l_temp = 'X'
      THEN
         RETURN (TRUE);
      ELSE
         RETURN (FALSE);
      END IF;
   EXCEPTION
      WHEN OTHERS
      THEN
         RETURN (FALSE);
   END;

   --
   -- To validate a password for OMS.
   FUNCTION validate_password (
      p_password         IN       VARCHAR2,
      p_min_passwd_len   OUT      NUMBER
      )
      RETURN NUMBER
   IS
      --
      --
      l_min_passwd_len   NUMBER;
      --
      --
      l_string           VARCHAR2(200);
   BEGIN
      BEGIN
         SELECT TO_NUMBER (profile_value)
           INTO l_min_passwd_len
           FROM SYSTEM_PROFILES
          WHERE profile_type = 'SYS'
            AND profile_code = 'MIN_PASS_LEN';
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            l_min_passwd_len := 1;
      END;

      -- Check the minimum password length.
      IF LENGTH (p_password) < l_min_passwd_len
      THEN
         p_min_passwd_len := l_min_passwd_len;
         RETURN (-1);
      END IF;

      -- Check password has at least 1 aplha and 1 numeric
      l_string := TRANSLATE (
                     UPPER (p_password),
                     'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789',
                     'XXXXXXXXXXXXXXXXXXXXXXXXXX0000000000'
                  );

      IF    INSTR (l_string, 'X') = 0
         OR INSTR (l_string, '0') = 0
      THEN
         RETURN (-2);
      END IF;

      RETURN (0);
   EXCEPTION
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (10, 'OMS', 'OMFVALPW', SQLERRM)
         );
   END;

   --
   -- To check that booking dates do not overlap with any other booking.
   FUNCTION check_booking_dates (
      p_offender_id_display   IN   VARCHAR2,
      p_offender_book_id      IN   NUMBER,
      p_booking_begin_date    IN   DATE,
      p_booking_end_date      IN   DATE
      )
      RETURN BOOLEAN
   IS
      --
      --
      l_exists   VARCHAR2(1);
   BEGIN
      SELECT 'X'
        INTO l_exists
        FROM dual
       WHERE EXISTS (SELECT 'X'
                       FROM OFFENDER_BOOKINGS b, OFFENDERS o
                      WHERE o.offender_id_display = p_offender_id_display
                        AND b.offender_id = o.offender_id
                        AND b.offender_book_id != p_offender_book_id
                        AND (  (   p_booking_begin_date != p_booking_end_date
                               AND p_booking_end_date != b.booking_end_date
                               AND p_booking_begin_date !=
                                      NVL (
                                         b.booking_end_date,
                                         TO_DATE ('01/01/4000', 'DD/MM/YYYY')
                                      )
                               AND p_booking_begin_date <=
                                      b.booking_begin_date
                               AND p_booking_end_date >=
                                      NVL (
                                         b.booking_end_date,
                                         TO_DATE ('01/01/4000', 'DD/MM/YYYY')
                                      ))
                            OR (  (   p_booking_begin_date >
                                         b.booking_begin_date
                                  AND p_booking_begin_date <
                                         NVL (
                                            b.booking_end_date,
                                            TO_DATE (
                                               '01/01/4000',
                                               'DD/MM/YYYY'
                                            )
                                         ))
                               OR (   p_booking_end_date >
                                         b.booking_begin_date
                                  AND p_booking_end_date <
                                         NVL (
                                            b.booking_end_date,
                                            TO_DATE (
                                               '01/01/4000',
                                               'DD/MM/YYYY'
                                            )
                                         ))
                               OR (   p_booking_begin_date <
                                         b.booking_begin_date
                                  AND p_booking_end_date >
                                         NVL (
                                            b.booking_end_date,
                                            TO_DATE (
                                               '01/01/4000',
                                               'DD/MM/YYYY'
                                            )
                                         )))));
      RETURN (TRUE);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN (FALSE);
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (10, 'OMS', 'OMFCBKDT', SQLERRM)
         );
   END;

   --
   FUNCTION get_previous_booking_no (
      p_offender_id_display        VARCHAR2,
      p_book_no               IN   VARCHAR2,
      p_booking_type          IN   VARCHAR2
      )
      RETURN VARCHAR2
   IS
      --
      --
      l_last_book_no   VARCHAR2(14);
   BEGIN
      IF p_book_no IS NULL
      THEN
         /* get the last booking id */
         SELECT b.booking_no
           INTO l_last_book_no
           FROM OFFENDER_BOOKINGS b, OFFENDERS o
          WHERE b.offender_id = o.offender_id
            AND o.offender_id_display = p_offender_id_display
            AND b.booking_type = p_booking_type
            AND b.booking_begin_date =
                   (SELECT MAX (b1.booking_begin_date)
                      FROM OFFENDER_BOOKINGS b1, OFFENDERS o1
                     WHERE b1.offender_id = o1.offender_id
                       AND o1.offender_id_display = p_offender_id_display
                       AND b1.booking_type = p_booking_type);
      ELSE
         SELECT b.booking_no
           INTO l_last_book_no
           FROM OFFENDER_BOOKINGS b, OFFENDERS o
          WHERE b.offender_id = o.offender_id
            AND o.offender_id_display = p_offender_id_display
            AND b.booking_type = p_booking_type
            AND b.booking_begin_date =
                   (SELECT MAX (b1.booking_begin_date)
                      FROM OFFENDER_BOOKINGS b1, OFFENDERS o1
                     WHERE b1.offender_id = o1.offender_id
                       AND o1.offender_id_display = p_offender_id_display
                       AND b1.booking_type = p_booking_type
                       AND b1.booking_begin_date <
                              (SELECT b2.booking_begin_date
                                 FROM OFFENDER_BOOKINGS b2, OFFENDERS o2
                                WHERE b2.offender_id = o2.offender_id
                                  AND o2.offender_id_display =
                                         p_offender_id_display
                                  AND b2.booking_no = p_book_no));
      END IF;

      RETURN (l_last_book_no);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN (NULL);
      WHEN TOO_MANY_ROWS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (
               10,
               'OMS',
               'OMFGPBKG',
               'Cannot identify a unique booking'
            )
         );
      WHEN OTHERS
      THEN
         RAISE_APPLICATION_ERROR (
            -20000,
            Oms_Utils.display_user_message (10, 'OMS', 'OMFGPBKG', SQLERRM)
         );
   END;
--
-- called by the system profiles screen OUMSYPFL when the record with profile type SYS
-- and profile code ROLE_PSWD is updated
-- it changes the password on the role TAG_USER
   PROCEDURE change_role_password (
      p_role_name   IN   VARCHAR2,
      p_password    IN   VARCHAR2
   )
   IS
      create_cursor   INTEGER;
      l_status        INTEGER;
   BEGIN
      --@@@Surya 29-Feb-2008:Td 8086:Removed double quotes
      create_cursor := DBMS_SQL.OPEN_CURSOR;
      DBMS_SQL.PARSE (
         create_cursor,
         'ALTER ROLE ' || p_role_name || ' IDENTIFIED BY ' || p_password,
         DBMS_SQL.v7
      );
      l_status := DBMS_SQL.EXECUTE (create_cursor);
   END;
----------------------------------------------------------------------------------------------
   /* Retrieves an offenders booking and name details */
   FUNCTION get_offender_booking_rec (p_offender_book_id IN OFFENDER_BOOKINGS.offender_book_id%TYPE)
   RETURN g_offender_booking_rec
   IS
   --
      v_offender_booking_rec g_offender_booking_rec;

      CURSOR get_off_book_cur
      IS
         SELECT o.offender_id,
         	o.offender_id_display,
         	o.last_name,
         	o.first_name,
         	o.middle_name,
         	ob.root_offender_id
           FROM OFFENDERS o, OFFENDER_BOOKINGS ob
          WHERE ob.offender_book_id = p_offender_book_id
            AND o.offender_id = ob.offender_id;
   --
   BEGIN
      --
      OPEN get_off_book_cur;
     FETCH get_off_book_cur INTO v_offender_booking_rec;
     CLOSE get_off_book_cur;

      RETURN v_offender_booking_rec;
      --
   END get_offender_booking_rec;
--*******************************************************************
-- THIS FUNCTION WILL BE USED IN MULTIPLE CASE NOTE REPORT(CN02).
--*******************************************************************
FUNCTION get_username (usrname VARCHAR2)
   RETURN VARCHAR2
IS
   lname   VARCHAR2(75);

   CURSOR get_name
   IS
      SELECT b.last_name || ', ' || b.first_name
        FROM staff_user_accounts a, staff_members b
       WHERE a.staff_user_type = 'GENERAL'
         AND a.staff_id = b.staff_id
         AND a.username = usrname;
BEGIN
   OPEN get_name;

   FETCH get_name
    INTO lname;

   CLOSE get_name;

   IF lname IS NULL
   THEN
      RETURN (usrname);
   ELSE
      RETURN lname;
   END IF;
END;


PROCEDURE get_staff_member_dtls (
 p_username         IN      STAFF_USER_ACCOUNTS.username%TYPE,
 p_last_name        OUT     STAFF_MEMBERS.last_name%TYPE,
 p_first_name       OUT     STAFF_MEMBERS.first_name%TYPE,
 p_middle_name      OUT     STAFF_MEMBERS.middle_name%TYPE,
 p_status           OUT     STAFF_MEMBERS.status%TYPE,
 p_staff_id         OUT     STAFF_MEMBERS.staff_id%TYPE
 )
 IS

 BEGIN

   SELECT sm.last_name,
          sm.first_name,
          sm.middle_name,
          sm.status,
          sm.staff_id
    INTO p_last_name,
         p_first_name,
         p_middle_name,
         p_status,
         p_staff_id
    FROM STAFF_USER_ACCOUNTS sua,
         STAFF_MEMBERS sm
   WHERE p_username = sua.username
     AND sua.staff_id = sm.staff_id;


 EXCEPTION
     WHEN OTHERS
     THEN
        Tag_Error.handle;
 END get_staff_member_dtls;



PROCEDURE delete_stakeholder_account (p_stakeholder_id IN stakeholder_accounts.stakeholder_id%TYPE)
IS
BEGIN

   DELETE FROM user_stakeholder_caseloads
   WHERE stakeholder_id = p_stakeholder_id;

   DELETE FROM stakeholder_caseloads
    WHERE stakeholder_id = p_stakeholder_id;

   DELETE FROM stakeholder_roles
    WHERE stakeholder_id = p_stakeholder_id;

   UPDATE staff_user_accounts
      SET stakeholder_id = NULL
    WHERE stakeholder_id = p_stakeholder_id;

   DELETE FROM stakeholder_accounts
    WHERE stakeholder_id = p_stakeholder_id;

EXCEPTION
     WHEN OTHERS
     THEN
        Tag_Error.handle;
END;



END Oms_Utils;
/
show err
