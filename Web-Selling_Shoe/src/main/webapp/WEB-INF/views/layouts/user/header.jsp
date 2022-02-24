<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/views/layouts/taglib.jsp"%>
<%@ page import="com.springmvc.Security.CustomSuccesHandler"%>
<c:set var="lang" value="${pageContext.response.locale}" />


<header class="header-section">
	<div class="header-top">
		<div class="container">
			<div class="ht-right">
				<security:authorize access="isAnonymous()">
					<a href='<c:url value="/dang-nhap" />' class="login-panel"><i
						class="fa fa-user"></i> <spring:message code="login" /></a>
				</security:authorize>
				<security:authorize access="isAuthenticated()">
					<a href='<c:url value="/dang-xuat" />' class="login-panel"><%=CustomSuccesHandler.getPrincipal().getFullName()%>
						| <spring:message code="logout" /> </a>
				</security:authorize>
				<div class="lan-selector">
					<div class="header__top__right__language" style="">
						<div>
							<c:choose>
								<c:when test="${lang=='en'}">
									<a>English</a>
								</c:when>
								<c:otherwise>
									<a>VietNam</a>
								</c:otherwise>
							</c:choose>
						</div>
						<span class="arrow_carrot-down"></span>
						<ul>
							<li><a href="?language=en">English</a></li>
							<li><a href="?language=vi_VN">VietNam</a></li>

						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="container">
		<div class="inner-header">
			<div class="row">
				<div class="col-lg-2 col-md-2">
					<div class="logo">
						<a href="./*"> <img src="template/user/img/logo.png" alt="">
						</a>
					</div>
				</div>
				<div class="col-lg-7 col-md-7">
					<div class="advanced-search">
						<button type="button" class="category-btn">All Categories</button>
						<div class="input-group">
							<input type="text" placeholder="What do you need?">
							<button type="button">
								<i class="ti-search"></i>
							</button>
						</div>
					</div>
				</div>
				<!-- Gio Hang Begin -->
				<div class="col-lg-3 text-right col-md-3">
					<ul class="nav-right">
						<li class="cart-icon"><a href="#"> <i
								class="icon_bag_alt"></i> ${TotalQuantyCart }
						</a>
							<div class="cart-hover">
								<div class="select-items">
									<table>
										<tbody>
											<c:forEach var="item" items="${Cart }">
												<tr>
													<td class="si-pic"><img
														src='<c:url value="/template/user/img/cart-page/${item.value.product.img }" />'
														alt=""></td>
													<td class="si-text">
														<div class="product-selected">
															<p>x${item.value.quanty }
															<h6>${item.value.product.name }</h6>
															<fmt:formatNumber pattern="#,##0 vnđ"
																value="${item.value.product.price }" />
															</p>
														</div>
													</td>
													<td class="si-close"><a
														href='<c:url value="/deletecart/${item.key }" />'><i
															class="ti-close"></i></a></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
								<div class="select-total">
									<span>total:</span>
									<h5>
										<fmt:formatNumber pattern="#,##0 vnđ"
											value="${TotalPriceCart }" />
									</h5>
								</div>
								<div class="select-button">
									<a href='<c:url value="/gio-hang" />'
										class="primary-btn view-card"><spring:message
											code="viewcard" /></a> <a href='<c:url value="/checkout" />'
										class="primary-btn checkout-btn"><spring:message
											code="checkout" /></a>
								</div>
							</div></li>
						<li class="cart-price"><fmt:formatNumber pattern="#,##0 vnđ"
								value="${TotalPriceCart }" /></li>
					</ul>
				</div>
				<!-- Gio Hang End -->
			</div>
		</div>
	</div>
	<div class="nav-item">
		<div class="container">
			<nav class="nav-menu mobile-menu">
				<ul>
					<li class="active"><a href='<c:url value="/trang-chu" />'><spring:message
								code="home" /></a></li>
					<li><a href='<c:url value="/shop" />'><spring:message
								code="shop" /></a></li>
					<li><a href='<c:url value="/blog" />'>Blog</a></li>
					<li><a href='<c:url value="/contact" />'><spring:message
								code="contact" /></a></li>
				</ul>
			</nav>
			<div id="mobile-menu-wrap"></div>
		</div>
	</div>
</header>
