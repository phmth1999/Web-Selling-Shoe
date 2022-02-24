package com.springmvc.Controller.User;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ContactController {
	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	public ModelAndView contact(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("user/contact/contact");
		return mav;
	}
}
