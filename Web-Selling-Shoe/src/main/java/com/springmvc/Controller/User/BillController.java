package com.springmvc.Controller.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.springmvc.Dto.CartDto;
import com.springmvc.Entity.Bill;
import com.springmvc.Entity.User;
import com.springmvc.Service.User.BillServiceImpl;
import com.springmvc.Service.User.CartServiceImpl;

@Controller
public class BillController {

	@Autowired
	private BillServiceImpl billServiceImpl;

	@Autowired
	private CartServiceImpl cartService;

	@Autowired
	private JavaMailSender mailSender;

	Locale lc = new Locale("vi", "VN");
	NumberFormat numf = NumberFormat.getCurrencyInstance(lc);

	@RequestMapping(value = "/checkout", method = RequestMethod.GET)
	public ModelAndView CheckOut() {
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails) {
			ModelAndView mav = new ModelAndView("user/bill/checkout");
			mav.addObject("bill", new Bill());
			return mav;
		} else {
			ModelAndView mav = new ModelAndView("user/account/login");
			mav.addObject("user", new User());
			return mav;
		}
	}

	@RequestMapping(value = "/checkout", method = RequestMethod.POST)
	public String CheckOutBill(HttpServletRequest request, HttpSession session, @ModelAttribute("bill") Bill bill)
			throws Exception {

		@SuppressWarnings("unchecked")
		HashMap<Integer, CartDto> cart = (HashMap<Integer, CartDto>) session.getAttribute("Cart");
		int quanty = cartService.TotalQuanty(cart);
		double total = cartService.TotalPrice(cart);

		session.setAttribute("hoadon", bill);
		session.setAttribute("Cart", cart);
		session.setAttribute("TotalQuantyCart", quanty);
		session.setAttribute("TotalPriceCart", total);

		String dirFile = request.getServletContext().getRealPath("HoaDon");
		System.out.println(dirFile);
		File fileDir = new File(dirFile);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		String hoadon = fileDir + "/HoaDon.pdf";
		session.setAttribute("hd", hoadon);

		// tao file pdf hoa don
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, new FileOutputStream(new File(hoadon)));
			// open
			document.open();

			Paragraph p = new Paragraph();
			p.add("BILLS");
			p.setAlignment(Element.ALIGN_CENTER);
			document.add(p);

			document.add(new Paragraph("FullName: " + bill.getFullname()));
			document.add(new Paragraph("Address: " + bill.getAddress()));
			document.add(new Paragraph("Phone: " + bill.getPhone()));
			document.add(new Paragraph("Date: " + java.time.LocalDate.now()));

			PdfPTable t = new PdfPTable(3);
			t.setSpacingBefore(25);
			t.setSpacingAfter(25);

			PdfPCell c1 = new PdfPCell(new Phrase("Quantity"));
			t.addCell(c1);
			PdfPCell c2 = new PdfPCell(new Phrase("Name"));
			t.addCell(c2);
			PdfPCell c3 = new PdfPCell(new Phrase("Price"));
			t.addCell(c3);

			for (Map.Entry<Integer, CartDto> itemCart : cart.entrySet()) {
				t.addCell("" + itemCart.getValue().getQuanty());
				t.addCell("" + itemCart.getValue().getProduct().getName());
				t.addCell("" + numf.format(itemCart.getValue().getTotalPrice()));
			}
			document.add(t);

			Paragraph tong = new Paragraph();
			tong.add("Toatal: " + numf.format(total));
			tong.setAlignment(Element.ALIGN_RIGHT);
			document.add(tong);

			document.add(new Paragraph(" "));

			Paragraph n = new Paragraph();
			n.add("Day...Month...Year");
			n.add("                                                                                                ");
			n.add("Day...Month...Year");
			document.add(n);

			Paragraph m = new Paragraph();
			m.add("          Buyer");
			m.add("                                                                                                ");
			m.add("                     Seller");
			document.add(m);

			// close
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// gui mail dinh kem file pdf
		MimeMessage message = mailSender.createMimeMessage();
		boolean multipart = true;
		MimeMessageHelper helper = new MimeMessageHelper(message, multipart);
		helper.setTo(bill.getUser());
		helper.setSubject("Bills");
		FileSystemResource fileHoaDon = new FileSystemResource(new File(hoadon));
		helper.addAttachment("HoaDon.pdf", fileHoaDon);
		String hashHD = getMD5File(new File(hoadon));
		session.setAttribute("hashHD", hashHD);
		helper.setText("Code hash md5 file HoaDon.pdf: " + hashHD + ". Link download: " +"https://drive.google.com/drive/u/1/folders/18ECHJCudAoWvNIrO4Mh0NzKaUCoEs8zL");
		mailSender.send(message);

		return "redirect:sign";
	}

	public static String getMD5File(File file) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(file);
			byte[] dataBytes = new byte[1024];
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			byte[] byteData = md.digest();
			fis.close();
			return convertByteToHex(byteData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String convertByteToHex(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	@RequestMapping(value = "/sign", method = RequestMethod.GET)
	public String sign() {
		return "user/bill/checksign";
	}

	@RequestMapping(value = "/sign", method = RequestMethod.POST)
	public String sign1(@RequestParam("sign") String sign, @RequestParam("key") String key, Model model,
			HttpSession session) throws Exception {
		@SuppressWarnings("unchecked")
		HashMap<Integer, CartDto> cart = (HashMap<Integer, CartDto>) session.getAttribute("Cart");
		int quanty = cartService.TotalQuanty(cart);
		double total = cartService.TotalPrice(cart);
		Bill b = (Bill) session.getAttribute("hoadon");
		String hd = (String) session.getAttribute("hd");
		String hashHD = (String) session.getAttribute("hashHD");
		
		String hoadon = hd;

		if (VerSign(sign, hoadon, key) == true) {
			b.setSign(sign);
			b.setPubkey(key);
			b.setData(hashHD);
			billServiceImpl.addBill(b, quanty, total);
			billServiceImpl.addBillDetail(b.getId(), cart);

			session.removeAttribute("Cart");
			session.removeAttribute("TotalQuantyCart");
			session.removeAttribute("TotalPriceCart");
			return "redirect:trang-chu";
		} else {
			model.addAttribute("erro", "Chữ ký không chính xác");
			return "user/bill/checksign";
		}
	}

	private static PublicKey readPublicKey(String key) throws Exception {
		byte[] b = Base64.getDecoder().decode(key);

		X509EncodedKeySpec spec = new X509EncodedKeySpec(b);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey pubKey = factory.generatePublic(spec);
		return pubKey;
	}

	public boolean VerSign(String sign, String data, String PublicKey) throws Exception {
		PublicKey pubKey = readPublicKey(PublicKey);

		byte[] signToVer = Base64.getDecoder().decode(sign);

		Signature rsa = Signature.getInstance("SHA256withRSA");
		rsa.initVerify(pubKey);

		FileInputStream datafis = new FileInputStream(new File(data));
		BufferedInputStream bis = new BufferedInputStream(datafis);
		byte[] input = new byte[1024];
		int len;
		while ((len = bis.read(input)) != -1) {
			rsa.update(input, 0, len);
		}
		;
		bis.close();

		boolean verfile = rsa.verify(signToVer);
		return verfile;
	}
}
