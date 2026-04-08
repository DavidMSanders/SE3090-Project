package onetoone.Calendar;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles all Calendar and recurring events endpoints
 */

@RestController
@RequestMapping("/api/v1")
public class CalendarController {
    @Autowired
    private CalendarEventRepository eventRepo;

    @Autowired
    private RecurrenceRuleRepository ruleRepo;

    @Autowired
    private EventParticipantRepository participantRepo;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> body){
        Map<String, Object> result = new HashMap<>();
        try {
            Long creatorId = Long.valueOf(body.get("userId").toString());
            String eventLabel = body.get("title").toString();
            LocalDate scheduleDate = LocalDate.parse(body.get("date").toString());
            String eventNotes = body.containsKey("notes") ? body.get("notes").toString() : null;

            CalendarEvent newEvent = new CalendarEvent(creatorId, eventLabel, scheduleDate, eventNotes);

            if(body.containsKey("startTime") && body.get("startTime") != null){
                newEvent.setOpenTime(java.time.LocalTime.parse(body.get("startTime").toString()));
            }

            newEvent = eventRepo.save(newEvent);
            result.put("id", newEvent.getEventKey());
            result.put("userId", newEvent.getCreatorId());
            result.put("title", newEvent.getEventLabel());
            result.put("date", newEvent.getScheduledDate().toString());
            result.put("notes", newEvent.getEventNotes());
            result.put("message", "Event created successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e){
            result.put("message", "Error creating event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<List<Map<String, Object>>> getAllEvents(@PathVariable Long userId){
        List<CalendarEvent> eventList = eventRepo.findByCreatorId(userId);
        return ResponseEntity.ok(buildEventList(eventList));
    }

    @GetMapping("/users/{userId}/events/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getupcomingEvents(@PathVariable Long userId){
        List<CalendarEvent> eventList = eventRepo.findByCreatorIdAndScheduledDateGreaterThanEqual(userId, LocalDate.now());
        return ResponseEntity.ok(buildEventList(eventList));
    }

    @GetMapping("/users/{userId}/events/month")
    public ResponseEntity<List<Map<String, Object>>> getEventsByMonth(@PathVariable Long userId, @RequestParam int year, @RequestParam int month){

        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        List<CalendarEvent> eventList = eventRepo.findByCreatorIdAndScheduledDateBetween(userId, periodStart, periodEnd);
        return ResponseEntity.ok(buildEventList(eventList));
    }

    @GetMapping("/users/{userId}/events/day")
    public ResponseEntity<List<Map<String, Object>>> getEventsByDay(@PathVariable Long userId, @RequestParam int year, @RequestParam int month, @RequestParam int day){
        LocalDate date = LocalDate.of(year, month, day);
        List<CalendarEvent> events = eventRepo.findByCreatorIdAndScheduledDate(userId, date);
        return ResponseEntity.ok(buildEventList(events));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable Long eventId){
    Map<String, Object> result = new HashMap<>();
    CalendarEvent event = eventRepo.findById(eventId).orElse(null);
    if(event == null){
        result.put("message", "Event not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    return ResponseEntity.ok(buildEventMap(event));
}

    @PutMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable Long eventId, @RequestBody Map<String, Object> body){
    Map<String, Object> result = new HashMap<>();
    CalendarEvent foundEvent = eventRepo.findById(eventId).orElse(null);
    if (foundEvent == null){
        result.put("message", "Event not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    if(body.containsKey("title")){
        foundEvent.setEventLabel(body.get("title").toString());
    }
    if(body.containsKey("date")){
        foundEvent.setScheduledDate(LocalDate.parse)(body.get("date").toString()));
    }
    if (body.containsKey("notes")){
        foundEvent.setEventNotes(body.get("notes").toString());
    }

    event = eventRepo.save(event);
    result.put("message", "Event updated sucessfully");
    result.putAll(buildEventMap(event));
    return ResponseEntity.ok(result);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable Long eventId) {
        Map<String, Object> result = new HashMap<>();
        CalendarEvent event = eventRepo.findById(eventId).orElse(null);
        if (event == null) {
            result.put("message", "Event not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        participantRepo.deleteByEventRef(eventId);
        eventRepo.delete(event);
        result.put("message", "Event deleted sucessfully");
        result.put("eventId", eventId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/events/recurring")
    public ResponseEntity<Map<String, Object>> createRecurringEvent(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long creatorId = Long.valueOf(body.get("userId").toString());
            String ruleLabel = body.get("title").toString();
            String ruleNotes = body.containsKey("notes") ? body.get("notes").toString(): null;
            String repeatDay = body.get("dayOfWeek").toString().toUpperCase();

            RecurrenceRule newRule = new RecurrenceRule();
            newRule.setOwnerId(creatorId);
            newRule.setRuleLabel(ruleLabel);
            newRule.setRuleNotes(ruleNotes);
            newRule.setRepeatDay(repeatDay);
            newRule = ruleRepo.save(newRule);

            DayOfWeek targetDay = DayOfWeek.valueOf(repeatDay);
            LocalDate currentDate = LocalDate.now();
            while (currentDate.getDayOfWeek() != targetDay) {
                currentDate = currentDate.plusDays(1);
            }

            DayOfWeek targetDay = DayOfWeek.valueOf(repeatDay);
            LocalDate currentDate = LocalDate.now();
            while (currentDate.getDayOfWeek() != targetDay) {
                currentDate = currentDate.plusDays(1);
            }

            int createdCount = 0;
            for (int weekNum = 0;
                 weekNum < 12;
                 weekNum++) {
                CalendarEvent newEvent = new CalendarEvent(creatorId, ruleLabel, currentDate, ruleNotes);
                newEvent.setRuleRef(newRule.getRuleKey());
                eventRepo.save(newEvent);
                currentDate = currentDate.plusWeeks(1);
                createdCount++;
            }

            result.put("reccurrenceId", newRule.getRuleKey());
            result.put("userId", creatorId);
            result.put("daysOfWeek", repeatDay);
            result.put("createdCount", createdCount);
            result.put("message", "Recurrence events created sucessfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("message", "Error creating recurring event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);


        }
    }

    @GetMapping("/users/{userId}/events/recurring")
    public ResponseEntity<List<Map<String, Object>>> getRecurringEvents(@PathVariable Long userId) {
        List<RecurrenceRule> ruleList = ruleRepo.findByOwnerId(userId);
        List<Map<String, Object>> rule = new ArrayList<>();
        for (RecurrenceRule rule : ruleList) {
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put("recurrenceId", rule.getRuleKey());
            ruleMap.put("userId", rule.getOwnerId());
            ruleMap.put("title", rule.getRuleLabel());
            ruleMap.put("notes", rule.getRuleNotes());
            ruleMap.put("dayOfWeek", rule.getRepeatDay());
            result.add(ruleMap);
        }
        return ResponseEntity.ok(result);

        @DeleteMapping("/events/recurring/{recurrenceId}")
        public ResponseEntity<Map<String, Object>> deleteRecurringEvent (@PathVariable Long recurrenceId){
            Map<String, Object> result = new HashMap<>();
            List<CalendarEvent> linkedEvents = eventRepo.findByRuleRef(recurrenceId);
            int deletedCount = linkedEvents.size();
            for (CalendarEvent evt : linkedEvents) {
                participantRepo.deleteByEventRef(evt.getEventKey());
                eventRepo.delete(evt);
            }
            ruleRepo.deleteById(recurrenceId);
            result.put("message", "Recurring events deleted successfully");
            result.put("recurrenceId", recurrenceId);
            result.put("deletedCount", deletedCount);
            return ResponseEntity.ok(result);

        }

        @PostMapping("/events/{eventId}/participants")
        public ResponseEntity<Map<String, Object>> addParticipant (@PathVariable Long
        eventId, @RequestBody Map < String, Object > body){
            Map<String, Object> result = new HashMap<>();
            CalendarEvent foundEvent = eventRepo.findById(eventId).orElse(null);
            if (foundEvent == null) {
                result.put("message", "Event not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            Long attendeeId = Long.valueOf(body.get("userId").toString());
            if (participantRepo.existsByEventRefAndAttendeeId) (eventId, attendeeId){
                result.put("message", "User is already a participant");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            EventParticipant newParticipant = new EventParticipant(eventId, attendeeId);
            newParticipant = participantRepo.save(newParticipant);
            Optional<User> accountLookup = userRepo.findById(attendeeId.intValue());
            result.put("participantId", newParticipant.getParticipantKey());
            result.put("eventId", eventId);
            result.put("userId", attendeeId);
            result.put("username", accountLookup.map(User::getAccountName).orElse("Unknown"));

            result.put("status", newParticipant.getInviteStatus());
            result.put("message", "Participant added successfully");
            return ResponseEntity.ok(result);


        }

        @GetMapping("/events/{eventId}/participants")
        public ResponseEntity<List<Map<String, Object>>> getParticipants (@PathVariable Long eventId){
            List<EventParticipant> attendeeList = participantRepo.findByEventRef(eventId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (EventParticipant attendee : attendeeList) {
                Map<String, Object> attendeeMap = new HashMap<>();
                attendeeMap.put("participantId", attendee.getParticipantKey());
                attendeeMap.put("eventId", attendee.getEventRef());
                attendeeMap.put("userId", attendee.getAttendeeId());
                attendeeMap.put("status", attendee.getInviteStatus());
                Optional<User> accountLookup = userRepo.findById(attendee.getAttendeeId().intValue());
                attendeeMap.put("username", accountLookup.map(User::getAccountName).orElse("Uknown"));
                result.add(attendeeMap);

            }

            return ResponseEntity.ok(result);
        }

        @DeleteMapping("/events/{eventId}/participants/{userId}")
        public ResponseEntity<Map<String, Object>> removeParticipant (@PathVariable Long eventId, @PathVariable Long
        userId){
            Map<String, Object> result = new HashMap<>();
            List<EventParticipant> attendeeList = participantRepo.findByEventRef(eventId);
            EventParticipant foundAttendee = null;
            for (EventParticipant attendee : attendeeList){
                if (attendee.getAttendeeId().equals(userId)) {
                    foundAttendee = attendee;
                    break;
                }
            }

            if (foundAttendee == null) {
                result.put("message", "Participant not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            participantRepo.delete(foundAttendee);
            result.put("message", "Participant removed successfully");
            result.put("eventId", eventId);
            result.put("userId", userId);
            return ResponseEntity.ok(result);

        }

        @GetMapping("/users/{userId}/events/invited")
        public ResponseEntity<List<Map<String, Object>>> getInviteEvent (@PathVariable Long userId){
            List<EventParticipant> attendanceList = participantRepo.findByAttendeeId(userId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (EventParticipant attendance : attendanceList) {
                CalendarEvent foundEvent = eventRepo.findById(attendance.getEventRef()).orElse(null);
                if (foundEvent != null && !foundEvent.getCreatorId().equals(userId)) {
                    Map<String, Object> eventMap = buildEventMap(foundEvent);
                    eventMap.put("status", attendance.getInviteStatus());
                    result.add(eventMap);
                }
            }
            return ResponseEntity.ok(result);
        }

        private Map<String, Object> buildEventMap (CalendarEvent evt){
            Map<String, Object> map = new HashMap<>();
            map.put("id", evt.getEventKey());
            map.put("userId", evt.getCreatorId());
            map.put("title", evt.getEventLabel());
            map.put("date", evt.getScheduledDate().toString());
            map.put("notes", evt.getEventNotes());
            map.put("startTime", evt.getOpenTime() != null ? evt.getOpenTime().toString() : null);
            map.put("endTime", evt.getCloseTime() != null ? evt.getCloseTime().toString() : null);
            map.put("recurrenceId", evt.getRuleRef());
            return map;
        }

        private List<Map<String, Object>> buildEventList (List < CalendarEvent > eventList) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (CalendarEvent evt : eventList) {
                result.add(buildEventMap(evt));
            }
            return result;
        }
}
