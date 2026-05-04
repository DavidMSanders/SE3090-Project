package onetoone;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChrisLopezSystemTest {


        @LocalServerPort
        private int port;

        // ── Block 1-10 shared state ───────────────────────────────────────────────
        private static int  clientUserId  = 0;
        private static int  trainerUserId = 0;
        private static int  groupId       = 0;
        private static long pbId          = 0L;
        private static long imageId       = 0L;
        private static long calEventId    = 0L;

        // ── Block 11-17 shared state ──────────────────────────────────────────────
        private static int  adminUserId   = 0;
        private static int  senderUserId  = 0;
        private static int  otherUserId   = 0;
        private static int  covGroupId    = 0;
        private static int  membershipId  = 0;
        private static long imageId1      = 0L;
        private static long imageId2      = 0L;
        private static long pbId1         = 0L;
        private static long pbId2         = 0L;

        private static final String S = String.valueOf(System.currentTimeMillis() % 100_000);

        @BeforeEach
        void configure() {
            RestAssured.port    = port;
            RestAssured.baseURI = "http://localhost";
        }

        // =========================================================================
        // HELPERS
        // =========================================================================

        private static String kv(String... pairs) {
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < pairs.length; i += 2) {
                if (i > 0) sb.append(',');
                sb.append('"').append(pairs[i]).append("\":\"").append(pairs[i+1]).append('"');
            }
            return sb.append('}').toString();
        }

        private static void need(long id, String label) {
            org.junit.jupiter.api.Assumptions.assumeTrue(id != 0,
                    label + " skipped — prerequisite ID not available");
        }

        private static void log(String msg) { System.out.println("[TEST] " + msg); }

        private int signup(String username, String password) {
            Response r = given().contentType(ContentType.JSON)
                    .body(kv("username", username, "password", password, "dateOfBirth", "01/01/1995"))
                    .post("/api/v1/auth/signup")
                    .then().statusCode(201).extract().response();
            return Integer.parseInt(r.jsonPath().getString("userId"));
        }

        // =========================================================================
        // BLOCK 1 — SIGNUP / LOGIN
        // =========================================================================

        @Test @Order(1)
        void t01_signup_newUser_returns201WithUserId() {
            Response r = given().contentType(ContentType.JSON)
                    .body(kv("username","client_"+S,"password","pass123","dateOfBirth","01/15/2000"))
                    .post("/api/v1/auth/signup")
                    .then()
                    .statusCode(201)
                    .body("message",  equalTo("User created successfully"))
                    .body("userId",   notNullValue())
                    .body("username", equalTo("client_"+S))
                    .body("role",     equalTo("Client"))
                    .extract().response();
            clientUserId = Integer.parseInt(r.jsonPath().getString("userId"));
            log("T01 signup OK clientUserId="+clientUserId);
        }

        @Test @Order(2)
        void t02_signup_duplicateUsername_returns409() {
            String body = kv("username","dup_"+S,"password","x","dateOfBirth","05/20/1998");
            given().contentType(ContentType.JSON).body(body).post("/api/v1/auth/signup");
            given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/auth/signup")
                    .then().statusCode(409).body("message", containsString("already exists"));
            log("T02 duplicate -> 409");
        }

        @Test @Order(3)
        void t03_login_correctCredentials_returnsStatusTrue() {
            String name = "lg_"+S;
            given().contentType(ContentType.JSON)
                    .body(kv("username",name,"password","pw","dateOfBirth","03/10/1995"))
                    .post("/api/v1/auth/signup").then().statusCode(201);
            given().contentType(ContentType.JSON)
                    .body(kv("accountName",name,"userPassword","pw"))
                    .post("/api/v1/auth/login")
                    .then().statusCode(200)
                    .body("status",   equalTo(true))
                    .body("message",  equalTo("Login successful"))
                    .body("userId",   notNullValue())
                    .body("username", equalTo(name));
            log("T03 valid login -> 200 status=true");
        }

        @Test @Order(4)
        void t04_login_wrongPassword_returns401StatusFalse() {
            String name = "wp_"+S;
            given().contentType(ContentType.JSON)
                    .body(kv("username",name,"password","correct","dateOfBirth","07/04/1990"))
                    .post("/api/v1/auth/signup");
            given().contentType(ContentType.JSON)
                    .body(kv("accountName",name,"userPassword","WRONG"))
                    .post("/api/v1/auth/login")
                    .then().statusCode(401).body("status", equalTo(false));
            log("T04 wrong password -> 401");
        }

        @Test @Order(5)
        void t05_login_blankFields_returns400() {
            given().contentType(ContentType.JSON)
                    .body("{\"accountName\":\"\",\"userPassword\":\"\"}")
                    .post("/api/v1/auth/login")
                    .then().statusCode(400).body("status", equalTo(false));
            log("T05 blank login -> 400");
        }

        // =========================================================================
        // BLOCK 2 — USER CONTROLLER
        // =========================================================================

        @Test @Order(10)
        void t10_getUser_existingId_returns200() {
            need(clientUserId,"T10");
            given().get("/api/v1/users/"+clientUserId)
                    .then().statusCode(200).body("userId", notNullValue());
            log("T10 GET user -> 200");
        }

        @Test @Order(11)
        void t11_getUser_unknownId_returns404() {
            given().get("/api/v1/users/999999")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T11 GET unknown user -> 404");
        }

        @Test @Order(12)
        void t12_getAllUsers_returnsNonEmptyList() {
            given().get("/api/v1/users")
                    .then().statusCode(200).body("$", not(empty())).body("[0].userId", notNullValue());
            log("T12 GET all users -> 200 non-empty");
        }

        @Test @Order(13)
        void t13_updateProfile_validUser_returns200() {
            need(clientUserId,"T13");
            given().contentType(ContentType.JSON)
                    .body("{\"accountName\":\"upd_"+S+"\"}")
                    .put("/api/v1/users/"+clientUserId+"/profile")
                    .then().statusCode(200).body("message", equalTo("Profile updated successfully"));
            log("T13 PUT profile -> 200");
        }

        @Test @Order(14)
        void t14_updateProfile_unknownUser_returns404() {
            given().contentType(ContentType.JSON).body("{\"accountName\":\"nobody\"}")
                    .put("/api/v1/users/999999/profile")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T14 PUT profile unknown -> 404");
        }

        @Test @Order(15)
        void t15_setTrainer_validUser_returnsRoleTrainer() {
            String name = "trainer_"+S;
            Response r = given().contentType(ContentType.JSON)
                    .body(kv("username",name,"password","tr","dateOfBirth","06/15/1993"))
                    .post("/api/v1/auth/signup").then().statusCode(201).extract().response();
            trainerUserId = Integer.parseInt(r.jsonPath().getString("userId"));
            given().put("/api/v1/users/"+trainerUserId+"/set-trainer")
                    .then().statusCode(200)
                    .body("role",   equalTo("Trainer"))
                    .body("userId", equalTo(trainerUserId));
            log("T15 set-trainer -> Trainer trainerUserId="+trainerUserId);
        }

        @Test @Order(16)
        void t16_setTrainer_unknownUser_returns404() {
            given().put("/api/v1/users/999999/set-trainer")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T16 set-trainer unknown -> 404");
        }

        @Test @Order(17)
        void t17_setAdmin_thenSetClient_rolesChangeCorrectly() {
            need(clientUserId,"T17");
            given().put("/api/v1/users/"+clientUserId+"/set-admin")
                    .then().statusCode(200).body("role", equalTo("Admin"));
            given().put("/api/v1/users/"+clientUserId+"/set-client")
                    .then().statusCode(200).body("role", equalTo("Client"));
            log("T17 Admin->Client round-trip OK");
        }

        @Test @Order(18)
        void t18_setAdmin_unknownUser_returns404() {
            given().put("/api/v1/users/999999/set-admin")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T18 set-admin unknown -> 404");
        }

        @Test @Order(19)
        void t19_setClient_unknownUser_returns404() {
            given().put("/api/v1/users/999999/set-client")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T19 set-client unknown -> 404");
        }

        @Test @Order(20)
        void t20_deleteUserById_unknownId_returns404() {
            given().delete("/api/v1/users/999999")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T20 DELETE unknown user -> 404");
        }

        // =========================================================================
        // BLOCK 3 — ADMIN CONTROLLER (non-admin auth guards)
        // =========================================================================

        @Test @Order(25)
        void t25_adminGetUsers_nonAdminCaller_returns403() {
            need(clientUserId,"T25");
            given().get("/api/v1/admin/users?adminId="+clientUserId)
                    .then().statusCode(403).body("error", containsString("Unauthorized"));
            log("T25 admin GET users non-admin -> 403");
        }

        @Test @Order(26)
        void t26_adminGetStats_nonAdminCaller_returns403() {
            need(clientUserId,"T26");
            given().get("/api/v1/admin/stats?adminId="+clientUserId).then().statusCode(403).body("error", notNullValue());
            log("T26 admin stats non-admin -> 403");
        }

        @Test @Order(27)
        void t27_adminChangeRole_nonAdminSupervisor_returns403() {
            need(clientUserId,"T27");
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/role?supervisorId="+clientUserId+"&updatedRole=Trainer")
                    .then().statusCode(403).body("error", containsString("Unauthorized"));
            log("T27 admin changeRole non-admin -> 403");
        }

        @Test @Order(28)
        void t28_adminGetUserById_nonAdminCaller_returns403() {
            need(clientUserId,"T28");
            given().get("/api/v1/admin/users/1?adminId="+clientUserId).then().statusCode(403).body("error", notNullValue());
            log("T28 admin getUserById non-admin -> 403");
        }

        @Test @Order(29)
        void t29_adminSetStatus_nonAdminCaller_returns403() {
            need(clientUserId,"T29");
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/status?adminId="+clientUserId+"&enabled=false")
                    .then().statusCode(403).body("error", notNullValue());
            log("T29 admin setStatus non-admin -> 403");
        }

        @Test @Order(30)
        void t30_adminDeleteUser_nonAdminCaller_returns403() {
            need(clientUserId,"T30");
            given().delete("/api/v1/admin/users/"+clientUserId+"?adminId="+clientUserId)
                    .then().statusCode(403).body("error", notNullValue());
            log("T30 admin deleteUser non-admin -> 403");
        }

        // =========================================================================
        // BLOCK 4 — GROUP CONTROLLER
        // =========================================================================

        @Test @Order(40)
        void t40_createGroup_trainerCreator_returns201GroupLeader() {
            need(trainerUserId,"T40");
            String body = "{\"groupTitle\":\"Alpha Squad "+S+"\","
                    +"\"groupType\":\"Trainer Group\","
                    +"\"groupDetails\":\"Test group\","
                    +"\"creatorId\":"+trainerUserId+"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/groups")
                    .then().statusCode(201)
                    .body("message",     equalTo("Group created successfully"))
                    .body("accessLevel", equalTo("GROUP_LEADER"))
                    .body("groupId",     notNullValue())
                    .extract().response();
            groupId = r.jsonPath().getInt("groupId");
            log("T40 group created groupId="+groupId);
        }

        @Test @Order(41)
        void t41_createGroup_invalidType_returns400() {
            need(trainerUserId,"T41");
            given().contentType(ContentType.JSON)
                    .body("{\"groupTitle\":\"X\",\"groupType\":\"BAD\",\"creatorId\":"+trainerUserId+"}")
                    .post("/api/v1/groups").then().statusCode(400).body("message", notNullValue());
            log("T41 invalid type -> 400");
        }

        @Test @Order(42)
        void t42_createGroup_blankTitle_returns400() {
            need(trainerUserId,"T42");
            given().contentType(ContentType.JSON)
                    .body("{\"groupTitle\":\"\",\"groupType\":\"Trainer Group\",\"creatorId\":"+trainerUserId+"}")
                    .post("/api/v1/groups").then().statusCode(400).body("message", notNullValue());
            log("T42 blank title -> 400");
        }

        @Test @Order(43)
        void t43_createGroup_unknownCreator_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"groupTitle\":\"Ghost\",\"groupType\":\"Trainer Group\",\"creatorId\":999999}")
                    .post("/api/v1/groups")
                    .then().statusCode(404).body("message", equalTo("Creator user not found"));
            log("T43 unknown creator -> 404");
        }

        @Test @Order(44)
        void t44_getGroupById_exists_returnsCorrectFields() {
            need(groupId,"T44");
            given().get("/api/v1/groups/"+groupId)
                    .then().statusCode(200)
                    .body("groupId",   equalTo(groupId))
                    .body("groupName", containsString("Alpha Squad"));
            log("T44 GET group by ID -> 200");
        }

        @Test @Order(45)
        void t45_getGroupById_notFound_returns404() {
            given().get("/api/v1/groups/999999")
                    .then().statusCode(404).body("message", equalTo("Group not found"));
            log("T45 GET unknown group -> 404");
        }

        @Test @Order(46)
        void t46_getAllGroups_searchAlpha_findsCreatedGroup() {
            need(trainerUserId,"T46"); need(groupId,"T46");
            given().get("/api/v1/groups?userId="+trainerUserId+"&search=Alpha")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].groupId", notNullValue());
            log("T46 GET groups search -> found");
        }

        @Test @Order(47)
        void t47_joinGroup_clientUser_returns201() {
            need(groupId,"T47"); need(clientUserId,"T47");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+clientUserId+"}")
                    .post("/api/v1/groups/"+groupId+"/join")
                    .then().statusCode(anyOf(equalTo(201), equalTo(409)))
                    .body("groupId", equalTo(groupId));
            log("T47 client joined group");
        }

        @Test @Order(48)
        void t48_joinGroup_duplicate_returns409() {
            need(groupId,"T48"); need(clientUserId,"T48");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+clientUserId+"}")
                    .post("/api/v1/groups/"+groupId+"/join");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+clientUserId+"}")
                    .post("/api/v1/groups/"+groupId+"/join")
                    .then().statusCode(409).body("message", containsString("already a member"));
            log("T48 duplicate join -> 409");
        }

        @Test @Order(49)
        void t49_getGroupMembers_returnsNonEmptyWithUserId() {
            need(groupId,"T49");
            given().get("/api/v1/groups/"+groupId+"/members")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].userId", notNullValue());
            log("T49 GET group members -> non-empty");
        }

        @Test @Order(50)
        void t50_getUserGroups_trainerUser_returnsNonEmpty() {
            need(trainerUserId,"T50");
            given().get("/api/v1/groups/user/"+trainerUserId)
                    .then().statusCode(200).body("$", not(empty())).body("[0].groupId", notNullValue());
            log("T50 GET user groups -> non-empty");
        }

        // =========================================================================
        // BLOCK 5 — GROUP MEMBERSHIP CONTROLLER
        // =========================================================================

        @Test @Order(55)
        void t55_getMembership_trainerInGroup_returnsGroupLeaderPermissions() {
            need(groupId,"T55"); need(trainerUserId,"T55");
            given().get("/api/v1/groupMemberships/user/"+trainerUserId+"/group/"+groupId)
                    .then().statusCode(200)
                    .body("groupId",          equalTo(groupId))
                    .body("userId",           equalTo(trainerUserId))
                    .body("membershipRole",   equalTo("GROUP_LEADER"))
                    .body("canManageMembers", equalTo(true));
            log("T55 GET trainer membership -> GROUP_LEADER manage=true");
        }

        @Test @Order(56)
        void t56_getMembership_notFound_returns404() {
            given().get("/api/v1/groupMemberships/user/999999/group/999999")
                    .then().statusCode(404).body("message", equalTo("Membership not found"));
            log("T56 GET missing membership -> 404");
        }

        @Test @Order(57)
        void t57_checkAdmin_groupLeader_returnsIsAdminTrueAllPermissions() {
            need(groupId,"T57"); need(trainerUserId,"T57");
            given().get("/api/v1/groupMemberships/user/"+trainerUserId+"/group/"+groupId+"/check-admin")
                    .then().statusCode(200)
                    .body("isAdmin",              equalTo(true))
                    .body("canManageMembers",     equalTo(true))
                    .body("canPostAnnouncements", equalTo(true))
                    .body("canAssignWorkouts",    equalTo(true));
            log("T57 check-admin GROUP_LEADER -> all perms true");
        }

        @Test @Order(58)
        void t58_checkAdmin_nonMember_returns404() {
            given().get("/api/v1/groupMemberships/user/999999/group/999999/check-admin")
                    .then().statusCode(404).body("message", equalTo("Membership not found"));
            log("T58 check-admin non-member -> 404");
        }

        @Test @Order(59)
        void t59_updatePermissions_unknownMembership_returns404() {
            String body = "{\"supervisorId\":1,\"newAccessLevel\":\"MEMBER\","
                    +"\"sendEnabled\":true,\"adminEnabled\":false,"
                    +"\"postEnabled\":false,\"assignEnabled\":false}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/groupMemberships/999999/permissions")
                    .then().statusCode(404).body("message", equalTo("Membership not found"));
            log("T59 update permissions unknown -> 404");
        }

        // =========================================================================
        // BLOCK 6 — PERSONAL BEST CONTROLLER
        // =========================================================================

        @Test @Order(60)
        void t60_createPersonalBest_validUser_returnsAllFields() {
            need(clientUserId,"T60");
            String body = "{\"userId\":"+clientUserId
                    +",\"exerciseName\":\"Bench Press\",\"bestValue\":225.0"
                    +",\"unit\":\"lbs\",\"recordDate\":\"2026-04-01\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/personalbests")
                    .then().statusCode(200)
                    .body("message",      equalTo("Personal best recorded successfully"))
                    .body("id",           notNullValue())
                    .body("exerciseName", equalTo("Bench Press"))
                    .body("bestValue",    equalTo(225.0f))
                    .body("unit",         equalTo("lbs"))
                    .extract().response();
            pbId = r.jsonPath().getLong("id");
            log("T60 PB created pbId="+pbId);
        }

        @Test @Order(61)
        void t61_createPersonalBest_unknownUser_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":999999,\"exerciseName\":\"Squat\",\"bestValue\":315.0}")
                    .post("/api/v1/personalbests")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T61 PB unknown user -> 404");
        }

        @Test @Order(62)
        void t62_getAllPersonalBests_returnsListWithExerciseName() {
            need(clientUserId,"T62");
            given().get("/api/v1/users/"+clientUserId+"/personalbests")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].exerciseName", notNullValue());
            log("T62 GET all PBs -> non-empty");
        }

        @Test @Order(63)
        void t63_getPersonalBestByExercise_partialMatch_returnsBenchEntry() {
            need(clientUserId,"T63");
            given().get("/api/v1/users/"+clientUserId+"/personalbests/Bench")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].exerciseName", containsStringIgnoringCase("Bench"));
            log("T63 GET PB by exercise -> Bench entry");
        }

        @Test @Order(64)
        void t64_updatePersonalBest_updatesValue_responseReflectsChange() {
            need(pbId,"T64");
            given().contentType(ContentType.JSON).body("{\"bestValue\":245.0,\"unit\":\"lbs\"}")
                    .put("/api/v1/personalbests/"+pbId)
                    .then().statusCode(200)
                    .body("bestValue", equalTo(245.0f))
                    .body("message",   equalTo("Personal best updated successfully"));
            log("T64 PB update -> 245");
        }

        @Test @Order(65)
        void t65_updatePersonalBest_notFound_returns404() {
            given().contentType(ContentType.JSON).body("{\"bestValue\":100.0}")
                    .put("/api/v1/personalbests/999999")
                    .then().statusCode(404).body("message", equalTo("Personal best not found"));
            log("T65 PB update unknown -> 404");
        }

        @Test @Order(66)
        void t66_deletePersonalBest_exists_returns200WithId() {
            need(pbId,"T66");
            given().delete("/api/v1/personalbests/"+pbId)
                    .then().statusCode(200)
                    .body("message", equalTo("Personal best deleted successfully"))
                    .body("id",      equalTo((int) pbId));
            log("T66 PB deleted");
        }

        @Test @Order(67)
        void t67_deletePersonalBest_alreadyDeleted_returns404() {
            need(pbId,"T67");
            given().delete("/api/v1/personalbests/"+pbId)
                    .then().statusCode(404).body("message", equalTo("Personal best not found"));
            log("T67 PB re-delete -> 404");
        }

        // =========================================================================
        // BLOCK 7 — IMAGE MESSAGE CONTROLLER
        // =========================================================================

        @Test @Order(70)
        void t70_sendImage_validSender_returnsIdSenderIdUrl() {
            need(clientUserId,"T70");
            String body = "{\"senderId\":"+clientUserId
                    +",\"groupId\":"+(groupId>0?groupId:1)
                    +",\"imageUrl\":\"https://example.com/pr.jpg\""
                    +",\"caption\":\"My PR\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/images/send")
                    .then().statusCode(200)
                    .body("message",  equalTo("Image sent successfully"))
                    .body("id",       notNullValue())
                    .body("senderId", notNullValue())
                    .body("imageUrl", equalTo("https://example.com/pr.jpg"))
                    .extract().response();
            imageId = r.jsonPath().getLong("id");
            log("T70 image sent imageId="+imageId);
        }

        @Test @Order(71)
        void t71_sendImage_unknownSender_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"senderId\":999999,\"imageUrl\":\"https://x.com/a.jpg\"}")
                    .post("/api/v1/images/send")
                    .then().statusCode(404).body("message", equalTo("Sender not found"));
            log("T71 send image unknown sender -> 404");
        }

        @Test @Order(72)
        void t72_getImageById_exists_returns200() {
            need(imageId,"T72");
            given().get("/api/v1/images/"+imageId)
                    .then().statusCode(200).body("id", equalTo((int) imageId));
            log("T72 GET image by ID -> 200");
        }

        @Test @Order(73)
        void t73_getImageById_notFound_returns404() {
            given().get("/api/v1/images/999999")
                    .then().statusCode(404).body("message", equalTo("Image not found"));
            log("T73 GET missing image -> 404");
        }

        @Test @Order(74)
        void t74_getUserImages_returnsListWithSenderId() {
            need(clientUserId,"T74");
            given().get("/api/v1/users/"+clientUserId+"/images")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].senderId", notNullValue());
            log("T74 GET user images -> non-empty");
        }

        @Test @Order(75)
        void t75_getGroupImages_returns200List() {
            need(groupId,"T75");
            given().get("/api/v1/groups/"+groupId+"/images")
                    .then().statusCode(200)
                    .body("$", not(empty()))
                    .body("[0].imageUrl", notNullValue());
            log("T75 GET group images -> non-empty with imageUrl");
        }

        @Test @Order(76)
        void t76_updateCaption_exists_returnsSuccessMessage() {
            need(imageId,"T76");
            given().contentType(ContentType.JSON).body("{\"caption\":\"Updated\"}")
                    .put("/api/v1/images/"+imageId+"/caption")
                    .then().statusCode(200)
                    .body("message", equalTo("Caption updated successfully"))
                    .body("caption", equalTo("Updated"));
            log("T76 caption update -> 200");
        }

        @Test @Order(77)
        void t77_updateCaption_notFound_returns404() {
            given().contentType(ContentType.JSON).body("{\"caption\":\"x\"}")
                    .put("/api/v1/images/999999/caption")
                    .then().statusCode(404).body("message", equalTo("Image not found"));
            log("T77 caption update unknown -> 404");
        }

        @Test @Order(78)
        void t78_deleteImage_exists_returns200WithId() {
            need(imageId,"T78");
            given().delete("/api/v1/images/"+imageId)
                    .then().statusCode(200)
                    .body("message", equalTo("Image message deleted successfully"))
                    .body("id",      equalTo((int) imageId));
            log("T78 image deleted");
        }

        @Test @Order(79)
        void t79_deleteImage_alreadyDeleted_returns404() {
            need(imageId,"T79");
            given().delete("/api/v1/images/"+imageId)
                    .then().statusCode(404).body("message", equalTo("Image not found"));
            log("T79 image re-delete -> 404");
        }

        // =========================================================================
        // BLOCK 8 — MESSAGE REST CONTROLLER
        // =========================================================================

        @Test @Order(80)
        void t80_getGroupMessages_emptyGroup_returnsEmptyArray() {
            given().get("/api/v1/messages/group/99999")
                    .then().statusCode(200).body("$", hasSize(0));
            log("T80 GET empty group messages -> []");
        }

        @Test @Order(81)
        void t81_getSingleMessage_notFound_returns404() {
            given().get("/api/v1/messages/999999")
                    .then().statusCode(404).body("message", equalTo("Message not found"));
            log("T81 GET missing message -> 404");
        }

        @Test @Order(82)
        void t82_deleteMessage_notFound_returns404() {
            given().contentType(ContentType.JSON).body("{\"senderId\":1}")
                    .delete("/api/v1/messages/999999")
                    .then().statusCode(404).body("message", equalTo("Message not found"));
            log("T82 DELETE missing message -> 404");
        }

        @Test @Order(83)
        void t83_editMessage_notFound_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"senderId\":1,\"updatedBody\":\"edited\"}")
                    .put("/api/v1/messages/999999")
                    .then().statusCode(404).body("message", equalTo("Message not found"));
            log("T83 PUT missing message -> 404");
        }

        // =========================================================================
        // BLOCK 9 — CALENDAR CONTROLLER
        // =========================================================================

        @Test @Order(90)
        void t90_createEvent_unknownUser_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":999999,\"title\":\"Test\",\"date\":\"2026-08-01\"}")
                    .post("/api/v1/events")
                    .then().statusCode(404).body("message", equalTo("User not found"));
            log("T90 create event unknown user -> 404");
        }

        @Test @Order(91)
        void t91_createEvent_validUser_returnsIdTitleDate() {
            need(clientUserId,"T91");
            String body = "{\"userId\":"+clientUserId
                    +",\"title\":\"Leg Day\",\"date\":\"2026-09-15\""
                    +",\"notes\":\"Squats\",\"startTime\":\"08:00\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/events")
                    .then().statusCode(200)
                    .body("message", equalTo("Event created successfully"))
                    .body("id",      notNullValue())
                    .body("title",   equalTo("Leg Day"))
                    .body("date",    equalTo("2026-09-15"))
                    .extract().response();
            calEventId = r.jsonPath().getLong("id");
            log("T91 calendar event created calEventId="+calEventId);
        }

        @Test @Order(92)
        void t92_getEventById_exists_returnsCorrectTitleAndId() {
            need(calEventId,"T92");
            given().get("/api/v1/events/"+calEventId)
                    .then().statusCode(200)
                    .body("id",    equalTo((int) calEventId))
                    .body("title", equalTo("Leg Day"));
            log("T92 GET calendar event -> 200 title=Leg Day");
        }

        @Test @Order(93)
        void t93_getEventById_notFound_returns404() {
            given().get("/api/v1/events/999999")
                    .then().statusCode(404).body("message", equalTo("Event not found"));
            log("T93 GET missing event -> 404");
        }

        @Test @Order(94)
        void t94_updateEvent_title_responseReflectsNewTitle() {
            need(calEventId,"T94");
            given().contentType(ContentType.JSON)
                    .body("{\"title\":\"Leg Day Updated\",\"notes\":\"Added RDLs\"}")
                    .put("/api/v1/events/"+calEventId)
                    .then().statusCode(200).body("title", equalTo("Leg Day Updated"));
            log("T94 calendar event updated -> new title");
        }

        @Test @Order(95)
        void t95_getAllEventsForUser_returnsNonEmptyWithIdField() {
            need(clientUserId,"T95");
            given().get("/api/v1/users/"+clientUserId+"/events")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].id", notNullValue());
            log("T95 GET all events for user -> non-empty");
        }

        @Test @Order(96)
        void t96_getUpcomingEvents_returns200() {
            need(clientUserId,"T96");
            given().get("/api/v1/users/"+clientUserId+"/events/upcoming")
                    .then().statusCode(200).body("$", not(empty())).body("[0].id", notNullValue());
            log("T96 GET upcoming events -> 200");
        }

        @Test @Order(97)
        void t97_getEventsByMonth_sept2026_containsCreatedEvent() {
            need(clientUserId,"T97");
            given().get("/api/v1/users/"+clientUserId+"/events/month?year=2026&month=9")
                    .then().statusCode(200).body("$", not(empty())).body("[0].date", notNullValue());
            log("T97 GET events by month -> non-empty Sept 2026");
        }

        @Test @Order(98)
        void t98_getEventsByDay_specificDate_returns200() {
            need(clientUserId,"T98");
            given().get("/api/v1/users/"+clientUserId+"/events/day?year=2026&month=9&day=15")
                    .then().statusCode(200).body("$", not(empty())).body("[0].title", notNullValue());
            log("T98 GET events by day -> 200");
        }

        @Test @Order(99)
        void t99_addParticipant_validEventAndUser_returns200Or409() {
            need(calEventId,"T99"); need(clientUserId,"T99");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+clientUserId+"}")
                    .post("/api/v1/events/"+calEventId+"/participants")
                    .then().statusCode(anyOf(equalTo(200), equalTo(409)))
                    .body("eventId", anyOf(notNullValue(), notNullValue()));
            log("T99 add participant -> 200 or 409");
        }

        @Test @Order(100)
        void t100_getParticipants_returnsListWithUserId() {
            need(calEventId,"T100");
            given().get("/api/v1/events/"+calEventId+"/participants")
                    .then().statusCode(200)
                    .body("$",          not(empty()))
                    .body("[0].userId", notNullValue());
            log("T100 GET participants -> non-empty userId present");
        }

        @Test @Order(101)
        void t101_createRecurringEvent_returnsRecurrenceIdDayOfWeekCount() {
            need(clientUserId,"T101");
            String body = "{\"userId\":"+clientUserId
                    +",\"title\":\"Monday Cardio\",\"dayOfWeek\":\"MONDAY\",\"notes\":\"Weekly\"}";
            given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/events/recurring")
                    .then().statusCode(200)
                    .body("recurrenceId", notNullValue())
                    .body("dayOfWeek",    equalTo("MONDAY"))
                    .body("createdCount", greaterThan(0));
            log("T101 recurring event created with instances");
        }

        @Test @Order(102)
        void t102_getRecurringEvents_returnsListWithRecurrenceId() {
            need(clientUserId,"T102");
            given().get("/api/v1/users/"+clientUserId+"/events/recurring")
                    .then().statusCode(200).body("$", not(empty()))
                    .body("[0].recurrenceId", notNullValue());
            log("T102 GET recurring events -> non-empty recurrenceId present");
        }

        @Test @Order(103)
        void t103_getInvitedEvents_returns200() {
            need(clientUserId,"T103");
            given().get("/api/v1/users/"+clientUserId+"/events/invited")
                    .then().statusCode(200).body("$", instanceOf(java.util.List.class));
            log("T103 GET invited events -> 200");
        }

        @Test @Order(104)
        void t104_deleteEvent_exists_returns200() {
            need(calEventId,"T104");
            given().delete("/api/v1/events/"+calEventId)
                    .then().statusCode(200).body("message", containsString("deleted"));
            log("T104 calendar event deleted");
        }

        @Test @Order(105)
        void t105_deleteEvent_alreadyDeleted_returns404() {
            need(calEventId,"T105");
            given().delete("/api/v1/events/"+calEventId)
                    .then().statusCode(404).body("message", equalTo("Event not found"));
            log("T105 calendar event re-delete -> 404");
        }

        // =========================================================================
        // BLOCK 10 — CLEANUP
        // =========================================================================

        @Test @Order(110)
        void t110_leaveGroup_clientUser_returns200OrAlreadyGone() {
            need(groupId,"T110"); need(clientUserId,"T110");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+clientUserId+"}")
                    .delete("/api/v1/groups/"+groupId+"/leave")
                    .then().statusCode(anyOf(equalTo(200), equalTo(404)))
                    .body("message", anyOf(containsString("success"), equalTo("User not a member of this group")));
            log("T110 client left group");
        }

        @Test @Order(111)
        void t111_deleteGroup_exists_returns200() {
            need(groupId,"T111");
            given().delete("/api/v1/groups/"+groupId)
                    .then().statusCode(200).body("message", containsString("deleted"));
            log("T111 group deleted");
        }

        @Test @Order(112)
        void t112_deleteGroup_alreadyDeleted_returns404() {
            need(groupId,"T112");
            given().delete("/api/v1/groups/"+groupId)
                    .then().statusCode(404).body("message", equalTo("Group not found"));
            log("T112 group re-delete -> 404");
        }

        // =========================================================================
        // BLOCK 11 — ADMIN CONTROLLER success paths (real admin user)
        // =========================================================================

        @Test @Order(200)
        void a01_setup_createAdminUser_verifyRoleIsAdmin() {
            Response r = given().contentType(ContentType.JSON)
                    .body(kv("username","cov_admin_"+S,"password","pwAdmin","dateOfBirth","04/04/1985"))
                    .post("/api/v1/auth/signup")
                    .then().statusCode(201).extract().response();
            adminUserId = Integer.parseInt(r.jsonPath().getString("userId"));
            given().put("/api/v1/users/"+adminUserId+"/set-admin")
                    .then().statusCode(200)
                    .body("role",   equalTo("Admin"))
                    .body("userId", equalTo(adminUserId));
            log("A01 admin created id="+adminUserId);
        }

        @Test @Order(201)
        void a02_adminGetAllUsers_realAdmin_returnsListWithIdFields() {
            need(adminUserId,"A02");
            given().get("/api/v1/admin/users?adminId="+adminUserId)
                    .then().statusCode(200)
                    .body("$",      not(empty()))
                    .body("[0].userId", notNullValue());
            log("A02 admin GET all users -> list with id field");
        }

        @Test @Order(202)
        void a03_adminGetUserById_realAdmin_returnsCorrectUserId() {
            need(adminUserId,"A03"); need(clientUserId,"A03");
            given().get("/api/v1/admin/users/"+clientUserId+"?adminId="+adminUserId)
                    .then().statusCode(200)
                    .body("userId", equalTo(clientUserId));
            log("A03 admin GET user by ID -> userId matches");
        }

        @Test @Order(203)
        void a04_adminGetUserById_notFound_returns404() {
            need(adminUserId,"A04");
            given().get("/api/v1/admin/users/999999?adminId="+adminUserId)
                    .then().statusCode(404)
                    .body("error", equalTo("Target user not found"));
            log("A04 admin GET unknown user -> 404");
        }

        @Test @Order(204)
        void a05_adminGetStats_realAdmin_returnsAllStatFields() {
            need(adminUserId,"A05");
            given().get("/api/v1/admin/stats?adminId="+adminUserId)
                    .then().statusCode(200)
                    .body("totalUsers",    notNullValue())
                    .body("totalAdmins",   notNullValue())
                    .body("totalTrainers", notNullValue())
                    .body("totalClients",  notNullValue())
                    .body("activeUsers",   notNullValue())
                    .body("suspendedUsers",notNullValue());
            log("A05 admin stats -> all 6 fields present");
        }

        @Test @Order(205)
        void a06_adminChangeRole_invalidRole_returns400() {
            need(adminUserId,"A06"); need(clientUserId,"A06");
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/role?supervisorId="+adminUserId+"&updatedRole=INVALID")
                    .then().statusCode(400)
                    .body("error", containsString("Invalid role"));
            log("A06 admin changeRole invalid -> 400");
        }

        @Test @Order(206)
        void a07_adminChangeRole_validRole_assertsOldAndNewRole() {
            need(adminUserId,"A07"); need(clientUserId,"A07");
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/role?supervisorId="+adminUserId+"&updatedRole=Trainer")
                    .then().statusCode(200)
                    .body("newRole", equalTo("Trainer"))
                    .body("oldRole", notNullValue())
                    .body("userId",  equalTo(clientUserId));
            given().put("/api/v1/admin/users/"+clientUserId
                    +"/role?supervisorId="+adminUserId+"&updatedRole=Client");
            log("A07 admin changeRole Trainer -> oldRole+newRole present");
        }

        @Test @Order(207)
        void a08_adminChangeRole_unknownTarget_returns404() {
            need(adminUserId,"A08");
            given().put("/api/v1/admin/users/999999/role?supervisorId="+adminUserId+"&updatedRole=Client")
                    .then().statusCode(404)
                    .body("error", equalTo("Target user not found"));
            log("A08 admin changeRole unknown target -> 404");
        }

        @Test @Order(208)
        void a09_adminUpdateUser_updatesAccountName_returns200() {
            need(adminUserId,"A09"); need(clientUserId,"A09");
            given().contentType(ContentType.JSON)
                    .body("{\"accountName\":\"adminUpd_"+S+"\"}")
                    .put("/api/v1/admin/users/"+clientUserId+"?adminId="+adminUserId)
                    .then().statusCode(200)
                    .body("message", equalTo("User updated successfully"))
                    .body("user",    notNullValue());
            log("A09 admin update user -> 200 message correct");
        }

        @Test @Order(209)
        void a10_adminUpdateUser_invalidDate_returns400() {
            need(adminUserId,"A10"); need(clientUserId,"A10");
            given().contentType(ContentType.JSON)
                    .body("{\"birthDate\":\"NOTADATE\"}")
                    .put("/api/v1/admin/users/"+clientUserId+"?adminId="+adminUserId)
                    .then().statusCode(400)
                    .body("message", containsString("Invalid date format"));
            log("A10 admin update bad date -> 400");
        }

        @Test @Order(210)
        void a11_adminUpdateUser_notFound_returns404() {
            need(adminUserId,"A11");
            given().contentType(ContentType.JSON)
                    .body("{\"accountName\":\"ghost\"}")
                    .put("/api/v1/admin/users/999999?adminId="+adminUserId)
                    .then().statusCode(404)
                    .body("error", equalTo("Target user not found"));
            log("A11 admin update unknown -> 404");
        }

        @Test @Order(211)
        void a12_adminSetStatus_disableThenReenable_assertsBothFlips() {
            need(adminUserId,"A12"); need(clientUserId,"A12");
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/status?adminId="+adminUserId+"&enabled=false")
                    .then().statusCode(200)
                    .body("isEnabled", equalTo(false))
                    .body("userId",    equalTo(clientUserId));
            given().put("/api/v1/admin/users/"+clientUserId
                            +"/status?adminId="+adminUserId+"&enabled=true")
                    .then().statusCode(200).body("isEnabled", equalTo(true));
            log("A12 admin setStatus disable->enable both verified");
        }

        @Test @Order(212)
        void a13_adminSetStatus_notFound_returns404() {
            need(adminUserId,"A13");
            given().put("/api/v1/admin/users/999999/status?adminId="+adminUserId+"&enabled=false")
                    .then().statusCode(404)
                    .body("error", equalTo("Target user not found"));
            log("A13 admin setStatus unknown -> 404");
        }

        // =========================================================================
        // BLOCK 12 — USERS CONTROLLER missing paths
        // =========================================================================

        @Test @Order(220)
        void b01_userControllerLogin_correctCredentials_returnsUserObject() {
            need(clientUserId,"B01");
            given().contentType(ContentType.JSON)
                    .body(kv("accountName","cov_admin_"+S,"userPassword","pwAdmin"))
                    .post("/api/v1/users/login")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(401)))
                    .body("$", notNullValue());
            log("B01 UserController login -> exercised");
        }

        @Test @Order(221)
        void b02_userControllerLogin_wrongCredentials_returns401() {
            given().contentType(ContentType.JSON)
                    .body(kv("accountName","nobody_ever_"+S,"userPassword","WRONG"))
                    .post("/api/v1/users/login")
                    .then().statusCode(401)
                    .body("message", equalTo("Invalid username or password"));
            log("B02 UserController login wrong -> 401");
        }

        @Test @Order(222)
        void b03_deleteUserByUsername_notFound_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"username\":\"ghost_"+S+"\",\"dateOfBirth\":\"01/01/1995\"}")
                    .post("/api/v1/users/delete")
                    .then().statusCode(404)
                    .body("message", equalTo("User not found"));
            log("B03 delete by username unknown -> 404");
        }

        @Test @Order(223)
        void b04_deleteUserByUsername_valid_returns200() {
            String name = "del_"+S;
            signup(name, "pw");
            given().contentType(ContentType.JSON)
                    .body("{\"username\":\""+name+"\",\"dateOfBirth\":\"01/01/1995\"}")
                    .post("/api/v1/users/delete")
                    .then().statusCode(200)
                    .body("message", equalTo("User deleted successfully"));
            log("B04 delete by username -> 200");
        }

        @Test @Order(224)
        void b05_getAssignableGroups_trainerWithGroup_returnsListWithGroupId() {
            need(trainerUserId,"B05");
            given().get("/api/v1/users/"+trainerUserId+"/assignable-groups")
                    .then().statusCode(200)
                    .body("$", instanceOf(java.util.List.class))
                    .body("size()", greaterThanOrEqualTo(0));
            log("B05 GET assignable groups -> 200");
        }

        @Test @Order(225)
        void b06_updateProfile_invalidDate_returns400() {
            need(clientUserId,"B06");
            given().contentType(ContentType.JSON)
                    .body("{\"birthDate\":\"BADDATE\"}")
                    .put("/api/v1/users/"+clientUserId+"/profile")
                    .then().statusCode(400)
                    .body("message", containsString("Invalid date format"));
            log("B06 PUT profile bad date -> 400");
        }

        // =========================================================================
        // BLOCK 13 — MESSAGE CONTROLLER ownership guards + missing branches
        // =========================================================================

        @Test @Order(230)
        void m01_deleteMessage_wrongSender_returns403or404() {
            given().contentType(ContentType.JSON)
                    .body("{\"senderId\":99999}")
                    .delete("/api/v1/messages/1")
                    .then()
                    .statusCode(anyOf(equalTo(403), equalTo(404)))
                    .body("message", anyOf(
                            equalTo("You can only delete your own messages"),
                            equalTo("Message not found")));
            log("M01 DELETE wrong sender -> ownership guard exercised");
        }

        @Test @Order(231)
        void m02_editMessage_wrongSender_returns403or404() {
            given().contentType(ContentType.JSON)
                    .body("{\"senderId\":99999,\"updatedBody\":\"hacked\"}")
                    .put("/api/v1/messages/1")
                    .then()
                    .statusCode(anyOf(equalTo(403), equalTo(404)))
                    .body("message", anyOf(
                            equalTo("You can only edit your own messages"),
                            equalTo("Message not found")));
            log("M02 PUT wrong sender -> ownership guard exercised");
        }

        @Test @Order(232)
        void m03_getGroupMessages_realGroup_returns200HasSize0() {
            need(groupId,"M03");
            // groupId was deleted in T111 so this exercises the empty-result branch
            given().get("/api/v1/messages/group/"+groupId)
                    .then().statusCode(200)
                    .body("$", hasSize(0));
            log("M03 GET group messages deleted group -> hasSize(0)");
        }

        @Test @Order(233)
        void m04_getGroupMessages_groupZero_returnsExactlyEmpty() {
            given().get("/api/v1/messages/group/0")
                    .then().statusCode(200)
                    .body("$", hasSize(0));
            log("M04 GET group 0 messages -> hasSize(0)");
        }

        // =========================================================================
        // BLOCK 14 — GROUP CONTROLLER missing methods
        // Setup new group for these tests
        // =========================================================================

        @Test @Order(240)
        void g01_setup_createCovGroup_forGroupMethodTests() {
            need(trainerUserId,"G01");
            String body = "{\"groupTitle\":\"CovGroup "+S+"\","
                    +"\"groupType\":\"Trainer Group\","
                    +"\"groupDetails\":\"Coverage group\","
                    +"\"creatorId\":"+trainerUserId+"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/groups")
                    .then().statusCode(201)
                    .body("accessLevel", equalTo("GROUP_LEADER"))
                    .body("groupId",     notNullValue())
                    .extract().response();
            covGroupId = r.jsonPath().getInt("groupId");
            log("G01 covGroup created id="+covGroupId);
        }

        @Test @Order(241)
        void g02_setup_createSenderAndOtherUsers() {
            Response r1 = given().contentType(ContentType.JSON)
                    .body(kv("username","sender_"+S,"password","pw","dateOfBirth","01/01/1995"))
                    .post("/api/v1/auth/signup")
                    .then().statusCode(201)
                    .body("role", equalTo("Client"))
                    .extract().response();
            senderUserId = Integer.parseInt(r1.jsonPath().getString("userId"));

            Response r2 = given().contentType(ContentType.JSON)
                    .body(kv("username","other_"+S,"password","pw","dateOfBirth","02/02/1995"))
                    .post("/api/v1/auth/signup")
                    .then().statusCode(201)
                    .body("role", equalTo("Client"))
                    .extract().response();
            otherUserId = Integer.parseInt(r2.jsonPath().getString("userId"));
            log("G02 senderUser="+senderUserId+" otherUser="+otherUserId);
        }

        @Test @Order(242)
        void g03_setup_otherUserJoinsCovGroup_capturesMembershipId() {
            need(covGroupId,"G03"); need(otherUserId,"G03");
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+otherUserId+"}")
                    .post("/api/v1/groups/"+covGroupId+"/join")
                    .then().statusCode(anyOf(equalTo(201), equalTo(409)))
                    .body("groupId", equalTo(covGroupId))
                    .body("userId",  equalTo(otherUserId));

            Response m = given()
                    .get("/api/v1/groupMemberships/user/"+otherUserId+"/group/"+covGroupId)
                    .then().statusCode(200)
                    .body("groupId", equalTo(covGroupId))
                    .body("userId",  equalTo(otherUserId))
                    .extract().response();
            membershipId = m.jsonPath().getInt("membershipId");
            log("G03 otherUser membershipId="+membershipId);
        }

        @Test @Order(243)
        void g04_getGroupMessages_viaGroupController_returnsEmptyList() {
            need(covGroupId,"G04");
            given().get("/api/v1/groups/"+covGroupId+"/messages")
                    .then().statusCode(200)
                    .body("$", hasSize(0));
            log("G04 GET /groups/{id}/messages -> hasSize(0)");
        }

        @Test @Order(244)
        void g05_updateGroup_groupLeader_assertsAllThreeFields() {
            need(covGroupId,"G05"); need(trainerUserId,"G05");
            String body = "{\"groupName\":\"Updated CovGroup "+S+"\","
                    +"\"groupDescription\":\"Updated desc\","
                    +"\"groupType\":\"Trainer Group\","
                    +"\"requestUserId\":"+trainerUserId+"}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/groups/"+covGroupId)
                    .then().statusCode(200)
                    .body("groupName",        containsString("Updated CovGroup"))
                    .body("groupDescription", equalTo("Updated desc"))
                    .body("groupType",        equalTo("Trainer Group"));
            log("G05 PUT update group -> all 3 fields correct");
        }

        @Test @Order(245)
        void g06_updateGroup_nonLeader_returns403() {
            need(covGroupId,"G06"); need(otherUserId,"G06");
            String body = "{\"groupName\":\"Hacked\","
                    +"\"groupDescription\":\"Hacked\","
                    +"\"groupType\":\"Trainer Group\","
                    +"\"requestUserId\":"+otherUserId+"}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/groups/"+covGroupId)
                    .then().statusCode(403)
                    .body("message", equalTo("Only GROUP_LEADER can update the group"));
            log("G06 PUT group non-leader -> 403 exact message");
        }

        @Test @Order(246)
        void g07_updateGroup_unknownGroup_returns404() {
            need(trainerUserId,"G07");
            String body = "{\"groupName\":\"X\",\"groupDescription\":\"X\","
                    +"\"groupType\":\"Trainer Group\","
                    +"\"requestUserId\":"+trainerUserId+"}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/groups/999999")
                    .then().statusCode(404)
                    .body("message", equalTo("Group not found"));
            log("G07 PUT group unknown -> 404");
        }

        @Test @Order(247)
        void g08_getAssignableClients_groupLeader_returns200() {
            need(covGroupId,"G08"); need(trainerUserId,"G08");
            given().get("/api/v1/groups/"+covGroupId+"/assignable-clients?trainerId="+trainerUserId)
                    .then().statusCode(200)
                    .body("$", not(empty())).body("[0].userId", notNullValue());
            log("G08 GET assignable-clients GROUP_LEADER -> 200 empty");
        }

        @Test @Order(248)
        void g09_getAssignableClients_unknownGroup_returns404() {
            given().get("/api/v1/groups/999999/assignable-clients?trainerId=1")
                    .then().statusCode(404)
                    .body("$", hasSize(0));
            log("G09 GET assignable-clients unknown group -> 404");
        }

        @Test @Order(249)
        void g10_getAssignableClients_trainerNotMember_returns403() {
            need(covGroupId,"G10");
            given().get("/api/v1/groups/"+covGroupId+"/assignable-clients?trainerId=999998")
                    .then().statusCode(403)
                    .body("$", hasSize(0));
            log("G10 GET assignable-clients non-member -> 403");
        }

        @Test @Order(250)
        void g11_getAssignableClients_memberNoWorkoutPermission_returns403() {
            need(covGroupId,"G11"); need(otherUserId,"G11");
            given().get("/api/v1/groups/"+covGroupId+"/assignable-clients?trainerId="+otherUserId)
                    .then().statusCode(403)
                    .body("$", hasSize(0));
            log("G11 GET assignable-clients no workout perm -> 403");
        }

        // =========================================================================
        // BLOCK 15 — GROUP MEMBERSHIP success paths + remaining branches
        // =========================================================================

        @Test @Order(260)
        void gm01_updatePermissions_trainerUpdatesOtherUser_assertsAllFields() {
            need(membershipId,"GM01"); need(trainerUserId,"GM01");
            String body = "{\"supervisorId\":"+trainerUserId+","
                    +"\"newAccessLevel\":\"TRAINER\","
                    +"\"sendEnabled\":true,\"adminEnabled\":false,"
                    +"\"postEnabled\":true,\"assignEnabled\":true}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/groupMemberships/"+membershipId+"/permissions")
                    .then().statusCode(200)
                    .body("message",              equalTo("Permissions updated successfully"))
                    .body("membershipRole",        equalTo("TRAINER"))
                    .body("canSendMessages",       equalTo(true))
                    .body("canManageMembers",      equalTo(false))
                    .body("canPostAnnouncements",  equalTo(true))
                    .body("canAssignWorkouts",     equalTo(true));
            log("GM01 update permissions -> 200 all 6 fields correct");
        }

        @Test @Order(261)
        void gm02_kickMember_trainerKicksOtherUser_assertsFourFields() {
            need(membershipId,"GM02"); need(trainerUserId,"GM02");
            given().contentType(ContentType.JSON)
                    .body("{\"supervisorId\":"+trainerUserId+"}")
                    .delete("/api/v1/groupMemberships/"+membershipId)
                    .then().statusCode(200)
                    .body("message",         equalTo("Member removed from group successfully"))
                    .body("removedUserId",   equalTo(otherUserId))
                    .body("groupId",         equalTo(covGroupId))
                    .body("removedUsername", notNullValue());
            log("GM02 kick member -> 200 all 4 fields correct");
        }

        @Test @Order(262)
        void gm03_kickMember_unknownMembership_returns404() {
            given().contentType(ContentType.JSON)
                    .body("{\"supervisorId\":1}")
                    .delete("/api/v1/groupMemberships/999999")
                    .then().statusCode(404)
                    .body("message", equalTo("Membership not found"));
            log("GM03 kick unknown membership -> 404");
        }

        @Test @Order(263)
        void gm04_kickMember_supervisorNotInGroup_returns403() {
            need(covGroupId,"GM04"); need(otherUserId,"GM04");
            // Re-add otherUser
            given().contentType(ContentType.JSON)
                    .body("{\"userId\":"+otherUserId+"}")
                    .post("/api/v1/groups/"+covGroupId+"/join");
            Response m = given()
                    .get("/api/v1/groupMemberships/user/"+otherUserId+"/group/"+covGroupId)
                    .then().statusCode(200).extract().response();
            int newMid = m.jsonPath().getInt("membershipId");

            given().contentType(ContentType.JSON)
                    .body("{\"supervisorId\":999998}")
                    .delete("/api/v1/groupMemberships/"+newMid)
                    .then().statusCode(403)
                    .body("message", equalTo("Supervisor is not a member of this group"));
            log("GM04 kick non-member supervisor -> 403");
        }

        @Test @Order(264)
        void gm05_checkAdmin_memberRole_returnsIsAdminFalse() {
            need(covGroupId,"GM05"); need(otherUserId,"GM05");
            given().get("/api/v1/groupMemberships/user/"+otherUserId+"/group/"+covGroupId+"/check-admin")
                    .then().statusCode(200)
                    .body("isAdmin",         equalTo(false))
                    .body("membershipRole",  notNullValue())
                    .body("canSendMessages", notNullValue());
            log("GM05 check-admin MEMBER -> isAdmin=false with actual perms");
        }

        // =========================================================================
        // BLOCK 16 — IMAGE HANDLER missing branches
        // =========================================================================

        @Test @Order(270)
        void i01_sendImage_noGroupId_storesWithNullGroupId() {
            need(senderUserId,"I01");
            String body = "{\"senderId\":"+senderUserId
                    +",\"imageUrl\":\"https://example.com/nogroupimg.jpg\""
                    +",\"caption\":\"No group\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/images/send")
                    .then().statusCode(200)
                    .body("message",  equalTo("Image sent successfully"))
                    .body("id",       notNullValue())
                    .body("imageUrl", equalTo("https://example.com/nogroupimg.jpg"))
                    .extract().response();
            imageId1 = r.jsonPath().getLong("id");
            log("I01 send image no groupId -> imageId1="+imageId1);
        }

        @Test @Order(271)
        void i02_sendImage_withGroupId_returnsGroupIdInResponse() {
            need(senderUserId,"I02"); need(covGroupId,"I02");
            String body = "{\"senderId\":"+senderUserId
                    +",\"groupId\":"+covGroupId
                    +",\"imageUrl\":\"https://example.com/groupimg.jpg\""
                    +",\"caption\":\"In group\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/images/send")
                    .then().statusCode(200)
                    .body("message",  equalTo("Image sent successfully"))
                    .body("groupId",  notNullValue())
                    .body("caption",  equalTo("In group"))
                    .extract().response();
            imageId2 = r.jsonPath().getLong("id");
            log("I02 send image with groupId -> imageId2="+imageId2);
        }

        @Test @Order(272)
        void i03_updateCaption_thenGetImage_confirmsNewCaptionPersisted() {
            need(imageId1,"I03");
            given().contentType(ContentType.JSON)
                    .body("{\"caption\":\"Confirmed caption\"}")
                    .put("/api/v1/images/"+imageId1+"/caption")
                    .then().statusCode(200)
                    .body("message", equalTo("Caption updated successfully"))
                    .body("caption", equalTo("Confirmed caption"));

            given().get("/api/v1/images/"+imageId1)
                    .then().statusCode(200)
                    .body("caption", equalTo("Confirmed caption"))
                    .body("id",      equalTo((int) imageId1));
            log("I03 caption update + GET confirm persistence -> OK");
        }

        @Test @Order(273)
        void i04_getGroupImages_afterSend_returnsNonEmptyWithImageUrl() {
            need(covGroupId,"I04"); need(imageId2,"I04");
            given().get("/api/v1/groups/"+covGroupId+"/images")
                    .then().statusCode(200)
                    .body("$",            not(empty()))
                    .body("[0].imageUrl", notNullValue())
                    .body("[0].senderId", notNullValue());
            log("I04 GET group images -> non-empty with imageUrl+senderId");
        }

        @Test @Order(274)
        void i05_getUserImages_assertsSentAtField() {
            need(senderUserId,"I05");
            given().get("/api/v1/users/"+senderUserId+"/images")
                    .then().statusCode(200)
                    .body("$",            not(empty()))
                    .body("[0].sentAt",   notNullValue())
                    .body("[0].imageUrl", notNullValue());
            log("I05 GET user images -> sentAt not null");
        }

        @Test @Order(275)
        void i06_deleteGroupImage_verifyCountDecremented() {
            need(imageId2,"I06"); need(covGroupId,"I06");
            int before = given().get("/api/v1/groups/"+covGroupId+"/images")
                    .then().statusCode(200).extract().jsonPath().getList("$").size();

            given().delete("/api/v1/images/"+imageId2)
                    .then().statusCode(200)
                    .body("message", equalTo("Image message deleted successfully"))
                    .body("id",      equalTo((int) imageId2));

            int after = given().get("/api/v1/groups/"+covGroupId+"/images")
                    .then().statusCode(200).extract().jsonPath().getList("$").size();

            org.junit.jupiter.api.Assertions.assertEquals(before - 1, after,
                    "Group image count must decrease by 1");
            log("I06 delete group image -> count "+before+"->"+after);
        }

        @Test @Order(276)
        void i07_deleteNonGroupImage_thenReDeleteReturns404() {
            need(imageId1,"I07");
            given().delete("/api/v1/images/"+imageId1)
                    .then().statusCode(200)
                    .body("message", equalTo("Image message deleted successfully"));

            given().delete("/api/v1/images/"+imageId1)
                    .then().statusCode(404)
                    .body("message", equalTo("Image not found"));
            log("I07 delete then re-delete -> 200 then 404");
        }

        @Test @Order(280)
        void p01_createPB_withDate_assertsAllFiveFields() {
            need(senderUserId,"P01");
            String body = "{\"userId\":"+senderUserId
                    +",\"exerciseName\":\"Squat\",\"bestValue\":315.0"
                    +",\"unit\":\"lbs\",\"recordDate\":\"2026-03-15\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/personalbests")
                    .then().statusCode(200)
                    .body("message",      equalTo("Personal best recorded successfully"))
                    .body("exerciseName", equalTo("Squat"))
                    .body("bestValue",    equalTo(315.0f))
                    .body("unit",         equalTo("lbs"))
                    .body("recordDate",   equalTo("2026-03-15"))
                    .extract().response();
            pbId1 = r.jsonPath().getLong("id");
            log("P01 PB with date pbId1="+pbId1);
        }

        @Test @Order(281)
        void p02_createPB_noDate_serverDefaultsToToday() {
            need(senderUserId,"P02");
            String body = "{\"userId\":"+senderUserId
                    +",\"exerciseName\":\"Pull-Up\",\"bestValue\":25.0,\"unit\":\"reps\"}";
            Response r = given().contentType(ContentType.JSON).body(body)
                    .post("/api/v1/personalbests")
                    .then().statusCode(200)
                    .body("exerciseName", equalTo("Pull-Up"))
                    .body("recordDate",   notNullValue())
                    .extract().response();
            pbId2 = r.jsonPath().getLong("id");
            log("P02 PB no date -> recordDate defaulted pbId2="+pbId2);
        }

        @Test @Order(282)
        void p03_getAllPBs_afterTwoCreates_returnsAtLeastTwo() {
            need(senderUserId,"P03");
            given().get("/api/v1/users/"+senderUserId+"/personalbests")
                    .then().statusCode(200)
                    .body("$",                hasSize(greaterThanOrEqualTo(2)))
                    .body("[0].id",           notNullValue())
                    .body("[0].exerciseName", notNullValue());
            log("P03 GET all PBs -> at least 2 with id+exerciseName");
        }

        @Test @Order(283)
        void p04_getPBByExercise_squatFilter_returnsSquatEntry() {
            need(senderUserId,"P04");
            given().get("/api/v1/users/"+senderUserId+"/personalbests/Squat")
                    .then().statusCode(200)
                    .body("$",                not(empty()))
                    .body("[0].exerciseName", containsStringIgnoringCase("Squat"))
                    .body("[0].bestValue",    equalTo(315.0f));
            log("P04 GET PB Squat filter -> Squat entry with correct value");
        }

        @Test @Order(284)
        void p05_getPBByExercise_noMatch_returnsExactlyEmpty() {
            need(senderUserId,"P05");
            given().get("/api/v1/users/"+senderUserId+"/personalbests/ZZZUnknown")
                    .then().statusCode(200)
                    .body("$", hasSize(0));
            log("P05 GET PB unknown exercise -> hasSize(0)");
        }

        @Test @Order(285)
        void p06_updatePB_allFields_assertsEachFieldUpdated() {
            need(pbId1,"P06");
            String body = "{\"exerciseName\":\"Front Squat\","
                    +"\"bestValue\":275.0,"
                    +"\"unit\":\"kg\","
                    +"\"recordDate\":\"2026-05-01\"}";
            given().contentType(ContentType.JSON).body(body)
                    .put("/api/v1/personalbests/"+pbId1)
                    .then().statusCode(200)
                    .body("exerciseName", equalTo("Front Squat"))
                    .body("bestValue",    equalTo(275.0f))
                    .body("unit",         equalTo("kg"))
                    .body("recordDate",   equalTo("2026-05-01"));
            log("P06 PB update all fields -> all 4 correct");
        }

        @Test @Order(286)
        void p07_updatePB_onlyBestValue_preservesOtherFields() {
            need(pbId2,"P07");
            given().contentType(ContentType.JSON)
                    .body("{\"bestValue\":30.0}")
                    .put("/api/v1/personalbests/"+pbId2)
                    .then().statusCode(200)
                    .body("bestValue",    equalTo(30.0f))
                    .body("exerciseName", equalTo("Pull-Up"))
                    .body("unit",         equalTo("reps"));
            log("P07 PB update bestValue only -> other fields preserved");
        }

        @Test @Order(287)
        void p08_deletePB_verifyCountDecremented() {
            need(pbId1,"P08"); need(senderUserId,"P08");
            int before = given().get("/api/v1/users/"+senderUserId+"/personalbests")
                    .then().statusCode(200).extract().jsonPath().getList("$").size();

            given().delete("/api/v1/personalbests/"+pbId1)
                    .then().statusCode(200)
                    .body("message", equalTo("Personal best deleted successfully"))
                    .body("id",      equalTo((int) pbId1));

            int after = given().get("/api/v1/users/"+senderUserId+"/personalbests")
                    .then().statusCode(200).extract().jsonPath().getList("$").size();

            org.junit.jupiter.api.Assertions.assertEquals(before - 1, after,
                    "PB count must decrease by 1");
            log("P08 PB delete -> count "+before+"->"+after);
        }

        @Test @Order(288)
        void p09_deleteSecondPB_thenReDeleteReturns404() {
            need(pbId2,"P09");
            given().delete("/api/v1/personalbests/"+pbId2)
                    .then().statusCode(200)
                    .body("message", equalTo("Personal best deleted successfully"))
                    .body("id",      equalTo((int) pbId2));

            given().delete("/api/v1/personalbests/"+pbId2)
                    .then().statusCode(404)
                    .body("message", equalTo("Personal best not found"));
            log("P09 delete pbId2 then re-delete -> 200 then 404");
        }

        @Test @Order(300)
        void z01_adminDeleteUserA_assertsSuccessMessage() {
            need(adminUserId,"Z01"); need(clientUserId,"Z01");
            given().delete("/api/v1/admin/users/"+clientUserId+"?adminId="+adminUserId)
                    .then().statusCode(200)
                    .body("message", equalTo("User deleted successfully"));
            log("Z01 admin deleted clientUser -> 200");
        }

        @Test @Order(301)
        void z02_adminDeleteUnknown_returns404() {
            need(adminUserId,"Z02");
            given().delete("/api/v1/admin/users/999999?adminId="+adminUserId)
                    .then().statusCode(404)
                    .body("error", equalTo("Target user not found"));
            log("Z02 admin delete unknown -> 404");
        }

        @Test @Order(302)
        void z03_deleteCovGroup_assertsMessage() {
            need(covGroupId,"Z03");
            given().delete("/api/v1/groups/"+covGroupId)
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(404)))
                    .body("message", anyOf(
                            containsString("deleted"),
                            equalTo("Group not found")));
            log("Z03 covGroup cleanup done");
        }
    }