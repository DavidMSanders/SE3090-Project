package coms309;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller used to showcase what happens when an exception is thrown
 *
 * @author Chris Lopez
 */

@RestController
class ExceptionController {

    @RequestMapping(method = RequestMethod.GET, path = "/member-not-found")
    public String triggerNotFoundError() {
        throw new NullPointerException("Error Member not found in system");
    }

    @RequestMapping(method = RequestMethod.GET, path = "/error-demo")
    public String triggerMemberError() {
        throw new RuntimeException("Error Unable to register member system error occurred");
    }

    @RequestMapping(method = RequestMethod.GET, path = "/invalid-member")
    public String triggerValidationError() {
        throw new IllegalArgumentException("Error Invalid member data username cannot be empty");
    }


}
