package com.springmvc.Controller.User;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.springmvc.Entity.User;
import com.springmvc.Service.User.UserServiceImpl;

@Controller
public class UserController {
	private static final Logger Log = Logger.getLogger(UserController.class);
	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private JavaMailSender mailSender;

	@RequestMapping(value = "/dang-nhap", method = RequestMethod.GET)
	public ModelAndView login() {
		Log.info("Đây là trang đăng nhập");
		ModelAndView mav = new ModelAndView("user/account/login");
		mav.addObject("user", new User());
		return mav;
	}

	@RequestMapping(value = "/dang-xuat", method = RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		Log.info("username:" + auth.getName() + "==>Đăng xuất thành công");
		return new ModelAndView("redirect:/trang-chu");
	}

	@RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
	public ModelAndView accessDenied() {
		return new ModelAndView("redirect:/dang-nhap?accessDenied");
	}

	@RequestMapping(value = "/dang-ky", method = RequestMethod.GET)
	public ModelAndView Register() {
		Log.info("Đây là trang đăng ký");
		ModelAndView mav = new ModelAndView("user/account/register");
		mav.addObject("user", new User());
		return mav;
	}

	@RequestMapping(value = "/dang-ky", method = RequestMethod.POST)
	public ModelAndView CreateAcc(@ModelAttribute("user") User user, HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("redirect:/xac-nhan");
		// Tao ma xac nhan email va luu vao session
		String maxn = generateRandomChars();
		session.setAttribute("maxn", maxn);
		// Gui ma xac nhan cho email
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getUsername());
		email.setSubject("website");
		email.setText(maxn);
		mailSender.send(email);
		// Luu lai vao session
		session.setAttribute("tk", user);
		return mav;
	}

	// Chuyen qua trang xac nhan ma
	@RequestMapping(value = "/xac-nhan", method = RequestMethod.GET)
	public String maxacnhan() {
		Log.info("Đây là trang mã xác nhận");
		return "user/account/xacnhan";
	}

	// Xu ly xac nhan ma de dang ky
	@RequestMapping(value = "/xac-nhan", method = RequestMethod.POST)
	public String xulymaxacnhan(HttpSession session, @RequestParam("maxacnhan") String maxacnhan, Model model) {
		String page = "";
		String maxn = (String) session.getAttribute("maxn");
		User tk = (User) session.getAttribute("tk");
		if (maxn.equals(maxacnhan)) {
			// luu lai khong tin
			userService.addAccount(tk);
			Log.info("username:" + tk.getUsername() + "==>Đăng ký thành công");
			page = "redirect:/dang-nhap";
		} else {
			model.addAttribute("erro", "Mã xác nhận không chính xác");
			Log.error("Mã xác nhận không chính xác");
			page = "user/account/xacnhan";
		}
		return page;
	}

	// Tao ma xac nhan email
	private String generateRandomChars() {
		// chuỗi chỉ định các ký tự
		String srcChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234556789";
		// độ dài của chuỗi mới
		int length = 5;
		// StringBuilder sử dụng để tạo chuỗi có thể thay đổi
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			// tạo một chuỗi mới từ các ký tự trong chuôi chỉ định
			sb.append(srcChars.charAt(random.nextInt(srcChars.length())));
		}
		return sb.toString();
	}

	@RequestMapping(value = "/checkUsername", method = RequestMethod.POST)
	public @ResponseBody String checkUsername(HttpServletRequest req, @ModelAttribute("user") User user) {
		return userService.findOneByUsername(user.getUsername());
	}
}
