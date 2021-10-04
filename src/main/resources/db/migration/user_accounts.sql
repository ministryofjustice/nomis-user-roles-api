create or replace package user_accounts
as
   function show_version return varchar2;

   procedure add_user(p_username       in varchar2,
                      p_last_name      in varchar2,
                      p_first_name     in varchar2,
                      p_middle_name    in varchar2 default null,
                      p_stakeholder_id in stakeholder_roles.stakeholder_id%type default null);

   procedure add_user_caseloads (p_username   in user_accessible_caseloads.username%type,
                                 p_caseloads  in varchar2);

   procedure add_user_caseload_roles(p_username   in user_accessible_caseloads.username%type,
                                     p_caseloads   in varchar2,
                                     p_roles       in varchar2);

   procedure remove_user_caseloads(p_username   in user_accessible_caseloads.username%type,
                                   p_caseloads   in varchar2);

   procedure add_stakeholder_roles(p_username   in user_accessible_caseloads.username%type);

   procedure add_all_stakeholder_roles( p_stakeholder_id in stakeholder_roles.stakeholder_id%type);

   procedure remove_stakeholder_roles(p_username   in user_accessible_caseloads.username%type,
                                      p_stakeholder_id in stakeholder_roles.stakeholder_id%type);

   procedure remove_all_stakeholder_roles( p_stakeholder_id in stakeholder_roles.stakeholder_id%type);

   procedure add_role_to_stkhldr_users(p_stakeholder_id in stakeholder_roles.stakeholder_id%type,
                                       p_role_id        in stakeholder_roles.role_id%type);

   procedure remove_role_from_stkhldr_users(p_stakeholder_id in stakeholder_roles.stakeholder_id%type,
                                            p_role_id        in stakeholder_roles.role_id%type);

   procedure add_csld_to_stkhldr_users(p_stakeholder_id in stakeholder_caseloads.stakeholder_id%type,
                                       p_caseload_id        in stakeholder_caseloads.caseload_id%type);

   procedure remove_csld_from_stkhldr_users(p_stakeholder_id in stakeholder_caseloads.stakeholder_id%type,
                                            p_caseload_id        in stakeholder_caseloads.caseload_id%type);

   procedure add_csld_to_stkhldr_users(p_stakeholder_id in user_stakeholder_caseloads.stakeholder_id%type,
                                       p_username       in user_stakeholder_caseloads.username%type,
                                       p_caseload_id    in user_stakeholder_caseloads.caseload_id%type);

   procedure remove_csld_from_stkhldr_users(p_stakeholder_id in user_stakeholder_caseloads.stakeholder_id%type,
                                            p_username       in user_stakeholder_caseloads.username%type,
                                            p_caseload_id    in user_stakeholder_caseloads.caseload_id%type);

   procedure add_user_and_roles(p_username    in varchar2,
                                p_last_name   in varchar2,
                                p_first_name  in varchar2,
                                p_middle_name in varchar2,
                                p_caseloads   in varchar2,
                                p_roles       in varchar2);

   procedure add_user_and_roles(p_username    in varchar2,
                                p_last_name   in varchar2,
                                p_first_name  in varchar2,
                                p_caseloads   in varchar2,
                                p_roles       in varchar2);

   procedure remove_user_account(p_username            in staff_user_accounts.username%type,
                                 p_remove_staff_member in varchar2 default null);

   function get_staff_id(p_user_account in staff_user_accounts.username%type) 
      return staff_user_accounts.staff_id%type;

   function check_caseloads_exist(p_caseloads in varchar2) return varchar2;

   function check_roles_exist(p_roles in varchar2) return varchar2;

   function stakeholder_id_exists(p_stakeholder_id in stakeholder_roles.stakeholder_id%type) return boolean;

   procedure add_staff_caseload_roles(p_staff_id   in staff_user_accounts.staff_id%type,
                                     p_caseloads   in varchar2,
                                     p_roles       in varchar2,
                                     p_user_type   in staff_user_accounts.staff_user_type%type default null);

   procedure remove_staff_caseloads(p_staff_id   in staff_user_accounts.staff_id%type,
                                    p_caseloads   in varchar2,
                                    p_user_type   in staff_user_accounts.staff_user_type%type default null);

   procedure remove_old_laa_caseloads(p_staff_id         in staff_user_accounts.staff_id%type,
                                      p_laa_code         in laa_locations.local_authority_code%type,
                                      p_user_type        in staff_user_accounts.staff_user_type%type default null,
                                      p_exclude_caseload in user_accessible_caseloads.caseload_id%type default null);
end;
/
show err
create or replace package body user_accounts
as
-- =============================================================
   v_version   CONSTANT VARCHAR2 ( 60 ) := '5.7   28-Jun-2021';
-- =============================================================
/*
  MODIFICATION HISTORY
   ------------------------------------------------------------------------------------------
   Person      Date           Version     Comments
   ---------   -----------    ---------   ---------------------------------------------------
   Paul M      28-Jun-2021     5.7        SDU-714 - Changed procedures add_staff_caseload_roles
                                                    and remove_staff_caseloads to include all
                                                    user accounts
   Paul M      09-Jun-2021     5.6        SDU-714 - Added new procedure remove_user_caseloads
   Paul M      28-May-2021     5.5        SDU-719 - Overload add_csld_to_stkhldr_users and 
                                                    remove_csld_from_stkhldr_users to include
                                                    parameter p_username
   Paul M      10-Feb-2021     5.4        SDU-694 - Change add_stakeholder_roles to add default
                                          caseload NWEB with role GLOBAL_SEARCH.
   Paul M      01-Feb-2021     5.3        SDU-694 - Check for non stakeholder roles in 
                                          remove_csld_from_stkhldr_users
   Paul M      25-Jan-2021     5.2        SDU-694 - Make add_role_to_stkhldr_users trigger friendly
   Paul M      05-Jan-2021     5.1        SDU-694 - Support new stakeholder form
   Paul M      15-Jan-2020     5.0        Initial version
*/

   username_exists exception;
   pragma exception_init(username_exists, -20101 );

   c_initial_password    constant varchar2(14) := 'welcome2cnomis';
   c_user_status         constant varchar2(6) := 'ACTIVE';
   c_default_caseload constant varchar2(4) := 'NWEB';
   c_default_roles     constant varchar2(13) := 'GLOBAL_SEARCH';


   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure add_user(p_username in varchar2,
                      p_last_name   in varchar2,
                      p_first_name  in varchar2,
                      p_middle_name in varchar2 default null,
                      p_stakeholder_id in stakeholder_roles.stakeholder_id%type default null)
   is
      v_username       varchar2(30);
      v_last_name      staff_members.last_name%type;
      v_first_name     staff_members.first_name%type;
      v_middle_name     staff_members.middle_name%type;
      v_staff_id       staff_members.staff_id%type;
   begin
      --
      -- If a stakeholder id has been provided - check its valid
      --
      if p_stakeholder_id is not null then
         if stakeholder_id_exists(p_stakeholder_id) = false then
            raise_application_error(-20103, 'stakeholder_id '||p_stakeholder_id||' does not exist');
         end if;
      end if;

      v_username := trim(upper(p_username));
      v_last_name := trim(upper(p_last_name));
      v_first_name := trim(upper(p_first_name));
      v_middle_name := trim(upper(p_middle_name));

      v_staff_id := get_staff_id(v_username);

      if v_staff_id is null then

         insert into staff_members(staff_id, first_name, last_name, middle_name, status)
                values (staff_id.nextval, v_first_name, v_last_name, v_middle_name, c_user_status)
                returning staff_id into v_staff_id;

         insert into staff_user_accounts(username, staff_id, staff_user_type, id_source, stakeholder_id )
                values(v_username, v_staff_id, 'GENERAL', 'USER', p_stakeholder_id);

         oms_utils.create_user( p_user_name  => v_username,
                                p_password   => c_initial_password);
         execute immediate 'alter user '||v_username||' password expire';
         commit;

      else
         raise_application_error(-20101, 'user account '||p_username||' already exists');
      end if;

   end add_user;

   procedure add_user_caseloads (p_username   in user_accessible_caseloads.username%type,
                                 p_caseloads   in varchar2)
   is
      v_username varchar2(30);
   begin
      v_username := trim(upper(p_username));

      if p_caseloads = 'ALL' then

         insert into user_accessible_caseloads (caseload_id, username, start_date)
         select cl.caseload_id, v_username, trunc(sysdate) 
           from caseloads cl
           join agency_locations al
             on al.agy_loc_id = cl.caseload_id
          where cl.caseload_type = 'INST' 
            and cl.active_flag = 'Y' 
            and cl.caseload_function = 'GENERAL' 
            and cl.trust_accounts_flag = 'Y' 
            and cl.caseload_id like '__I'
            and al.active_flag = 'Y'
            and not exists (select null 
                              from user_accessible_caseloads
                             where caseload_id = cl.caseload_id
                               and username = v_username);
      else

         insert into user_accessible_caseloads (caseload_id, username, start_date)
         select cl.caseload_id, v_username, trunc(sysdate)
           from ( select trim( substr (txt,
                           instr (txt, ',', 1, level  ) + 1,
                           instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as caseload_id
                    from (select ','||p_caseloads||',' txt
                            from dual)
                         connect by level <= length(p_caseloads)-length(replace(p_caseloads,',',''))+1) cl
           join caseloads c
             on c.caseload_id = cl.caseload_id
          where not exists (select null 
                              from user_accessible_caseloads
                             where caseload_id = cl.caseload_id
                               and username = v_username);
      end if;

   end add_user_caseloads;

   procedure add_user_caseload_roles(p_username   in user_accessible_caseloads.username%type,
                                     p_caseloads   in varchar2,
                                     p_roles       in varchar2)
   is
      v_username varchar2(30);
      v_dummy    varchar2(1);
   begin
      v_username := trim(upper(p_username));


      add_user_caseloads(p_username => v_username,
                         p_caseloads => p_caseloads);

      if p_caseloads = 'ALL' then
   
         insert into user_caseload_roles (role_id, username, caseload_id)
         select omr.role_id, uac.username, uac.caseload_id
           from user_accessible_caseloads uac
           join caseloads cl
             on cl.caseload_id = uac.caseload_id
                and cl.caseload_type = 'INST'
           join (select trim( substr (txt,
                                      instr (txt, ',', 1, level  ) + 1,
                                      instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as role_code
                   from (select ','||p_roles||',' txt
                           from dual)
                                connect by level <= length(p_roles)-length(replace(p_roles,',',''))+1) rl
             on 1=1
           join oms_roles omr
             on omr.role_code = rl.role_code
          where uac.username = v_username
            and not exists (select null
                              from user_caseload_roles
                             where role_id = omr.role_id
                               and username = v_username
                               and caseload_id = uac.caseload_id);

      else
 
         insert into user_caseload_roles (role_id, username, caseload_id)
         select omr.role_id, uac.username, uac.caseload_id
           from user_accessible_caseloads uac
           join caseloads cl
             on cl.caseload_id = uac.caseload_id
           join (select trim( substr (txt,
                                      instr (txt, ',', 1, level  ) + 1,
                                      instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as role_code
                   from (select ','||p_roles||',' txt
                           from dual)
                                connect by level <= length(p_roles)-length(replace(p_roles,',',''))+1) rl
             on 1=1
           join oms_roles omr
             on omr.role_code = rl.role_code
          where uac.username = v_username
            and uac.caseload_id in ( select trim( substr (txt,
                                                 instr (txt, ',', 1, level  ) + 1,
                                                 instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as caseload
                                      from (select ','||p_caseloads||',' txt
                                              from dual)
                                           connect by level <= length(p_caseloads)-length(replace(p_caseloads,',',''))+1) 
            and not exists (select null
                              from user_caseload_roles
                             where role_id = omr.role_id
                               and username = v_username
                               and caseload_id = uac.caseload_id);
      end if;
   end add_user_caseload_roles;

   procedure remove_user_caseloads(p_username   in user_accessible_caseloads.username%type,
                                   p_caseloads   in varchar2)
   is
      v_username user_accessible_caseloads.username%type;
      v_caseloads varchar2(4000);
      v_invalid_caseloads varchar2(4000);
      
   begin
      v_username := upper(trim(p_username));
      v_caseloads := upper(p_caseloads);

      v_invalid_caseloads := check_caseloads_exist(v_caseloads);

      if v_invalid_caseloads is not null then
         raise_application_error(-20104, 'Invalid caseload_id(s): '||v_invalid_caseloads);
      end if;

      delete 
        from user_caseload_roles ucr
       where ucr.username = v_username
         and ucr.caseload_id in ( select trim( substr (txt,
                                                 instr (txt, ',', 1, level  ) + 1,
                                                 instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as caseload
                                      from (select ','||v_caseloads||',' txt
                                              from dual)
                                           connect by level <= length(v_caseloads)-length(replace(v_caseloads,',',''))+1); 

      delete 
        from user_accessible_caseloads uac
       where username = v_username
         and uac.caseload_id in ( select trim( substr (txt,
                                                 instr (txt, ',', 1, level  ) + 1,
                                                 instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as caseload
                                      from (select ','||v_caseloads||',' txt
                                              from dual)
                                           connect by level <= length(v_caseloads)-length(replace(v_caseloads,',',''))+1); 


   end remove_user_caseloads;

   procedure add_stakeholder_roles(p_username   in user_accessible_caseloads.username%type)
   is
      v_stakeholder_id staff_user_accounts.stakeholder_id%type;
      v_username user_accessible_caseloads.username%type;
      
   begin
      v_username := upper(trim(p_username));

      select stakeholder_id
        into v_stakeholder_id
        from staff_user_accounts 
       where username = v_username;
      
      --    
      -- SDU-694 - add default caselaod and role(s), currently NWEB and GLOBAL_SEARCH
      -- if not already present.
      --    
      add_user_caseload_roles(p_username   => v_username,
                              p_caseloads  => c_default_caseload,
                              p_roles      => c_default_roles);

      --
      -- SDU-719 - change to use user_stakeholder_caseloads
      --
      insert into user_accessible_caseloads (caseload_id, username, start_date)
      select usc.caseload_id, v_username, trunc(sysdate) 
        from user_stakeholder_caseloads usc
       where usc.stakeholder_id = v_stakeholder_id
         and usc.username = v_username
         and not exists (select null 
                           from user_accessible_caseloads
                          where caseload_id = usc.caseload_id
                            and username = v_username);

      insert into user_caseload_roles (role_id, username, caseload_id)
      select sr.role_id, uac.username, uac.caseload_id
        from user_accessible_caseloads uac
        join user_stakeholder_caseloads usc
          on usc.caseload_id = uac.caseload_id
             and usc.username = uac.username
        join stakeholder_roles sr
          on sr.stakeholder_id = usc.stakeholder_id
       where uac.username = v_username
         and usc.stakeholder_id = v_stakeholder_id
         and not exists (select null
                           from user_caseload_roles
                          where role_id = sr.role_id
                            and username = uac.username
                            and caseload_id = uac.caseload_id);
   exception
      when no_data_found then
         raise_application_error(-20102, 'user account '||p_username||' not found');
               
   end add_stakeholder_roles;

   procedure add_all_stakeholder_roles( p_stakeholder_id in stakeholder_roles.stakeholder_id%type)
   is
      
   begin

      --
      -- SDU-719 - change to use user_stakeholder_caseloads
      --
      insert into user_accessible_caseloads (caseload_id, username, start_date)
      select usc.caseload_id, sua.username, trunc(sysdate) 
        from user_stakeholder_caseloads usc
        join staff_user_accounts sua
          on sua.stakeholder_id = usc.stakeholder_id
       where usc.stakeholder_id = p_stakeholder_id
         and not exists (select null 
                           from user_accessible_caseloads
                          where caseload_id = usc.caseload_id
                            and username = sua.username);

      insert into user_caseload_roles (role_id, username, caseload_id)
      select sr.role_id, uac.username, uac.caseload_id
        from user_accessible_caseloads uac
        join staff_user_accounts sua
          on sua.username = uac.username
        join user_stakeholder_caseloads usc
          on usc.caseload_id = uac.caseload_id
         and usc.stakeholder_id = sua.stakeholder_id
        join stakeholder_roles sr
          on sr.stakeholder_id = sua.stakeholder_id
       where sua.stakeholder_id = p_stakeholder_id
         and not exists (select null
                           from user_caseload_roles
                          where role_id = sr.role_id
                            and username = uac.username
                            and caseload_id = uac.caseload_id);
               
   end add_all_stakeholder_roles;

   procedure remove_stakeholder_roles(p_username   in user_accessible_caseloads.username%type,
                                      p_stakeholder_id in stakeholder_roles.stakeholder_id%type)
   is
      v_username user_accessible_caseloads.username%type;
      
   begin
      v_username := upper(trim(p_username));

      delete 
        from user_caseload_roles ucr
       where ucr.username = v_username
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = ucr.username
                        and sua.stakeholder_id = p_stakeholder_id)
         and ucr.role_id in (select role_id
                           from stakeholder_roles
                          where stakeholder_id = p_stakeholder_id);

      delete 
        from user_accessible_caseloads uac
       where username = v_username
         and caseload_id in (select caseload_id
                               from user_stakeholder_caseloads
                              where stakeholder_id = p_stakeholder_id
                                and username = v_username)
         and not exists (select null
                           from user_caseload_roles ucr
                          where ucr.username = uac.username
                            and ucr.caseload_id = uac.caseload_id); 


   end remove_stakeholder_roles;

   procedure remove_all_stakeholder_roles( p_stakeholder_id in stakeholder_roles.stakeholder_id%type)
   is
      
   begin

      delete 
        from user_caseload_roles ucr
       where ucr.role_id in (select role_id
                           from stakeholder_roles
                          where stakeholder_id = p_stakeholder_id)
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = ucr.username
                        and sua.stakeholder_id = p_stakeholder_id);

      delete 
        from user_accessible_caseloads uac
       where uac.caseload_id in (select caseload_id
                               from stakeholder_caseloads
                              where stakeholder_id = p_stakeholder_id)
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = uac.username
                        and sua.stakeholder_id = p_stakeholder_id)
         and not exists (select null
                           from user_caseload_roles ucr
                          where ucr.username = uac.username
                            and ucr.caseload_id = uac.caseload_id);

   end remove_all_stakeholder_roles;

   procedure add_role_to_stkhldr_users(p_stakeholder_id in stakeholder_roles.stakeholder_id%type,
                                       p_role_id        in stakeholder_roles.role_id%type)
   is
   begin
      insert into user_caseload_roles (role_id, username, caseload_id)
      select p_role_id, uac.username, uac.caseload_id
        from user_accessible_caseloads uac
        join staff_user_accounts sua
          on sua.username = uac.username
        join stakeholder_caseloads sc
          on sc.caseload_id = uac.caseload_id
         and sc.stakeholder_id = sua.stakeholder_id
       where sua.stakeholder_id = p_stakeholder_id;
   exception
      when dup_val_on_index then
         null;
   end add_role_to_stkhldr_users;

   procedure remove_role_from_stkhldr_users(p_stakeholder_id in stakeholder_roles.stakeholder_id%type,
                                            p_role_id        in stakeholder_roles.role_id%type)
   is
   begin
      delete 
        from user_caseload_roles ucr
       where ucr.role_id = p_role_id
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = ucr.username
                        and sua.stakeholder_id = p_stakeholder_id);
   end remove_role_from_stkhldr_users;

   procedure add_csld_to_stkhldr_users(p_stakeholder_id in stakeholder_caseloads.stakeholder_id%type,
                                       p_caseload_id        in stakeholder_caseloads.caseload_id%type)
   is
   begin
      insert into user_accessible_caseloads (caseload_id, username, start_date)
      select p_caseload_id, sua.username, trunc(sysdate) 
        from staff_user_accounts sua
       where sua.stakeholder_id = p_stakeholder_id
         and not exists (select null 
                           from user_accessible_caseloads
                          where caseload_id = p_caseload_id
                            and username = sua.username);

      insert into user_caseload_roles (role_id, username, caseload_id)
      select sr.role_id, sua.username, p_caseload_id
        from staff_user_accounts sua
        join stakeholder_roles sr
          on sr.stakeholder_id = sua.stakeholder_id
       where sua.stakeholder_id = p_stakeholder_id
         and not exists (select null
                           from user_caseload_roles
                          where role_id = sr.role_id
                            and username = sua.username
                            and caseload_id = p_caseload_id);
   end add_csld_to_stkhldr_users;

   procedure remove_csld_from_stkhldr_users(p_stakeholder_id in stakeholder_caseloads.stakeholder_id%type,
                                            p_caseload_id        in stakeholder_caseloads.caseload_id%type)
   is 
   begin
      delete 
        from user_caseload_roles ucr
       where ucr.caseload_id = p_caseload_id
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = ucr.username
                        and sua.stakeholder_id = p_stakeholder_id)
         and exists (select null
                       from stakeholder_roles sr
                      where sr.role_id = ucr.role_id
                        and sr.stakeholder_id = p_stakeholder_id);

      delete 
        from user_accessible_caseloads uac
       where uac.caseload_id = p_caseload_id
         and exists (select null
                       from staff_user_accounts sua
                      where sua.username = uac.username
                        and sua.stakeholder_id = p_stakeholder_id)
         and not exists (select null
                           from user_caseload_roles ucr
                          where ucr.username = uac.username
                            and ucr.caseload_id = p_caseload_id) ;
   end remove_csld_from_stkhldr_users;

   procedure add_csld_to_stkhldr_users(p_stakeholder_id in user_stakeholder_caseloads.stakeholder_id%type,
                                       p_username       in user_stakeholder_caseloads.username%type,
                                       p_caseload_id    in user_stakeholder_caseloads.caseload_id%type)
   is
   begin
      insert into user_accessible_caseloads (caseload_id, username, start_date)
      select p_caseload_id, p_username, trunc(sysdate) 
        from  dual
       where not exists (select null 
                           from user_accessible_caseloads
                          where caseload_id = p_caseload_id
                            and username = p_username);

      insert into user_caseload_roles (role_id, username, caseload_id)
      select sr.role_id, p_username, p_caseload_id
        from stakeholder_roles sr
       where sr.stakeholder_id = p_stakeholder_id
         and not exists (select null
                           from user_caseload_roles
                          where role_id = sr.role_id
                            and username = p_username
                            and caseload_id = p_caseload_id);
   end add_csld_to_stkhldr_users;

   procedure remove_csld_from_stkhldr_users(p_stakeholder_id in user_stakeholder_caseloads.stakeholder_id%type,
                                            p_username       in user_stakeholder_caseloads.username%type,
                                            p_caseload_id    in user_stakeholder_caseloads.caseload_id%type)
   is 
   begin
      delete 
        from user_caseload_roles ucr
       where ucr.caseload_id = p_caseload_id
         and ucr.username = p_username
         and exists (select null
                       from stakeholder_roles sr
                      where sr.role_id = ucr.role_id
                        and sr.stakeholder_id = p_stakeholder_id);

      delete 
        from user_accessible_caseloads uac
       where uac.caseload_id = p_caseload_id
         and uac.username = p_username
         and not exists (select null
                           from user_caseload_roles ucr
                          where ucr.username = uac.username
                            and ucr.caseload_id = p_caseload_id) ;
   end remove_csld_from_stkhldr_users;

   procedure add_user_and_roles(p_username    in varchar2,
                                p_last_name   in varchar2,
                                p_first_name  in varchar2,
                                p_middle_name in varchar2,
                                p_caseloads   in varchar2,
                                p_roles       in varchar2)
   is
      v_username varchar2(30);
      v_caseloads varchar2(4000);
      v_roles varchar2(4000);
      v_invalid_caseloads varchar2(4000);
      v_invalid_roles varchar2(4000);
   begin
      v_username  := trim(upper(p_username));
      v_caseloads := upper(p_caseloads);
      v_roles     := upper(p_roles);

      add_user(p_username    => v_username,
               p_last_name   => p_last_name,
               p_first_name  => p_first_name,
               p_middle_name => p_middle_name);

      if v_caseloads != 'ALL' then
         v_invalid_caseloads := check_caseloads_exist(v_caseloads);
         if v_invalid_caseloads is not null then
            dbms_output.put_line('WARNING: The following caseloads do not exist and will not be added to user '||v_username||': '|| v_invalid_caseloads);
         end if;
      end if;
            
      v_invalid_roles := check_roles_exist(v_roles);
      if v_invalid_roles is not null then
         dbms_output.put_line('WARNING: The following roles do not exist and will not be added to user '||v_username||': '|| v_invalid_roles);
      end if;

      add_user_caseload_roles(p_username    => v_username,
                              p_caseloads   => v_caseloads,
                              p_roles       => v_roles);

      commit;
      dbms_output.put_line('INFORMATION: '||v_username||' added');
   exception
      when username_exists then
         dbms_output.put_line('ERROR: '||sqlerrm);
   end add_user_and_roles;

   procedure add_user_and_roles(p_username    in varchar2,
                                p_last_name   in varchar2,
                                p_first_name  in varchar2,
                                p_caseloads   in varchar2,
                                p_roles       in varchar2)
   is
   begin
      add_user_and_roles(p_username    => p_username,
                         p_last_name   => p_last_name,
                         p_first_name  => p_first_name,
                         p_middle_name => null,
                         p_caseloads   => p_caseloads,
                         p_roles       => p_roles);
 
   end add_user_and_roles;


   procedure remove_user_account(p_username            in staff_user_accounts.username%type,
                                 p_remove_staff_member in varchar2 default null)
   is
      v_staff_id staff_members.staff_id%type;
      v_username varchar2(30);
   begin
      v_username := trim(upper(p_username));
      
      delete from user_caseload_roles
       where username = v_username;

      delete from user_accessible_caseloads     
       where username = v_username;

      delete from staff_user_accounts
       where username = v_username
      returning staff_id into v_staff_id;

      oms_utils.drop_user( p_user_name  => v_username);

      if p_remove_staff_member = 'Y' then
         delete from staff_members
          where staff_id = v_staff_id;
      end if;

   end remove_user_account;



   function get_staff_id(p_user_account in staff_user_accounts.username%type) 
      return staff_user_accounts.staff_id%type
   is
      v_staff_id staff_user_accounts.staff_id%type;
   
   begin

      select staff_id 
        into v_staff_id
        from staff_user_accounts 
       where username = p_user_account;

      return v_staff_id;
   exception
      when no_data_found then
         return null;
   end get_staff_id;

   function check_caseloads_exist(p_caseloads in varchar2) return varchar2
   is
      v_invalid_caseloads varchar2(4000);
   begin
      --
      -- Check that the caseloads exist and return cs list of those that don't
      --
      v_invalid_caseloads := null;
      for rec in (select trim( substr (txt,
                        instr (txt, ',', 1, level  ) + 1,
                        instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as caseload_id
                    from (select ','||p_caseloads||',' txt
                         from dual)
                      connect by level <= length(p_caseloads)-length(replace(p_caseloads,',',''))+1 
                   minus
                  select caseload_id
                    from caseloads)
      loop
         if v_invalid_caseloads is null then
            v_invalid_caseloads := rec.caseload_id;
         else
            v_invalid_caseloads := v_invalid_caseloads||','||rec.caseload_id;
         end if;
      end loop;
      return v_invalid_caseloads;
   end check_caseloads_exist;

   function check_roles_exist(p_roles in varchar2) return varchar2
   is
      v_invalid_roles varchar2(4000);
   begin
      --
      -- Check that the roles exist and return cs list of those that don't
      --
      for rec in (select trim( substr (txt,
                                       instr (txt, ',', 1, level  ) + 1,
                                       instr (txt, ',', 1, level+1) - instr (txt, ',', 1, level) -1 ) ) as role_code
                    from (select ','||p_roles||',' txt
                            from dual)
                   connect by level <= length(p_roles)-length(replace(p_roles,',',''))+1
                   minus
                  select omr.role_code
                    from oms_roles omr)
      loop
         if v_invalid_roles is null then
            v_invalid_roles := rec.role_code;
         else
            v_invalid_roles := v_invalid_roles||','||rec.role_code;
         end if;
      end loop;

      return v_invalid_roles;
   end check_roles_exist;

   function stakeholder_id_exists(p_stakeholder_id in stakeholder_roles.stakeholder_id%type) return boolean
   is
      v_dummy varchar2(1);
   begin
      select 'Y' 
        into v_dummy
        from stakeholder_accounts
       where stakeholder_id = p_stakeholder_id;

      return true;
   exception
      when no_data_found then
         return false;
   end stakeholder_id_exists;

   procedure add_staff_caseload_roles(p_staff_id   in staff_user_accounts.staff_id%type,
                                     p_caseloads   in varchar2,
                                     p_roles       in varchar2,
                                     p_user_type   in staff_user_accounts.staff_user_type%type default null)
   is
   begin 
      for ua_rec in ( select username
                        from staff_user_accounts
                       where staff_id = p_staff_id
                         and (p_user_type is null or staff_user_type = upper(p_user_type)))
      loop

         add_user_caseload_roles(p_username   => ua_rec.username,
                                 p_caseloads  => p_caseloads,
                                 p_roles      => p_roles);
      end loop;

   end add_staff_caseload_roles;

   procedure remove_staff_caseloads(p_staff_id   in staff_user_accounts.staff_id%type,
                                    p_caseloads   in varchar2,
                                    p_user_type   in staff_user_accounts.staff_user_type%type default null)
   is
   begin 

      for ua_rec in ( select username
                        from staff_user_accounts
                       where staff_id = p_staff_id
                         and (p_user_type is null or staff_user_type = upper(p_user_type)))
      loop

         remove_user_caseloads(p_username   => ua_rec.username,
                               p_caseloads  => p_caseloads);
      end loop;

   end remove_staff_caseloads;

   procedure remove_old_laa_caseloads(p_staff_id         in staff_user_accounts.staff_id%type,
                                      p_laa_code         in laa_locations.local_authority_code%type,
                                      p_user_type        in staff_user_accounts.staff_user_type%type default null,
                                      p_exclude_caseload in user_accessible_caseloads.caseload_id%type default null)
   is
      v_caseloads varchar2(4000);
   begin
      
      select listagg(agy_loc_id, ',')
             within group (order by local_authority_code) as caseloads
        into v_caseloads
        from laa_locations
       where local_authority_code = p_laa_code
         and (p_exclude_caseload is null or agy_loc_id != p_exclude_caseload)
       group by local_authority_code;

      remove_staff_caseloads(p_staff_id   => p_staff_id,
                             p_caseloads  => v_caseloads,
                             p_user_type  => p_user_type);
   
   exception
      when no_data_found then
         raise_application_error(-20106, 'local authority code '||p_laa_code||' not found');
   end remove_old_laa_caseloads;
end;
/
show err
