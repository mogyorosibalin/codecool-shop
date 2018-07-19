package com.codecool.shop.controller;

import com.braintreegateway.*;
import com.codecool.shop.dao.ElementNotFoundException;
import com.codecool.shop.dao.OrdersListDao;
import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.implementation.OrdersListDaoMem;
import com.codecool.shop.dao.implementation.ProductCategoryDaoMem;
import com.codecool.shop.dao.implementation.ProductDaoMem;
import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.model.Order;
import com.codecool.shop.model.Product;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@WebServlet(urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession();
        List<Product> cartItems;
        cartItems = (ArrayList)session.getAttribute("ShoppingCart");
        float totalPrice = 0;
        if (cartItems != null) {
            totalPrice = calculateTotalPrice(cartItems);
        }

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());
        WebContext context = new WebContext(req, resp, req.getServletContext());

        context.setVariable("cartItems", cartItems);
        context.setVariable("totalPrice", totalPrice);
        engine.process("product/checkout.html", context, resp.getWriter());

    }

    static float calculateTotalPrice(List<Product> productList) {
//
//        return productList.stream()
//                .map(Product::getDefaultPrice)
//
        float totalPrice = 0;
        for (Product product : productList) {
            totalPrice += product.getDefaultPrice() * product.getShoppingCartQuantity();
        }
        return totalPrice;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OrdersListDao ordersDataStore = OrdersListDaoMem.getInstance();
        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());
        WebContext context = new WebContext(req, resp, req.getServletContext());

        Order order = new Order();
        order.setName(req.getParameter("name"));
        order.setEmail(req.getParameter("email"));
        order.setPhoneNumber(req.getParameter("phone-number"));
        order.setBillingCountry(req.getParameter("billing-country"));
        order.setBillingCity(req.getParameter("billing-city"));
        order.setBillingZipCode(req.getParameter("billing-zip"));
        order.setBillingAddress(req.getParameter("billing-address"));
        order.setShippingCountry(req.getParameter("shipping-country"));
        order.setShippingCity(req.getParameter("shipping-city"));
        order.setShippingZipCode(req.getParameter("shipping-zip"));
        order.setShippingAddress(req.getParameter("shipping-address"));

        HttpSession session = req.getSession();
        List<Product> cartItems;
        cartItems = (ArrayList)session.getAttribute("ShoppingCart");
        float totalPrice = Float.parseFloat(req.getParameter("total-price"));
        if (cartItems != null) {
            for (Product product : cartItems) {
                order.add(product);
            }
        }
        order.setTotalPrice(totalPrice);

        session.setAttribute("currentOrder", order);
        ordersDataStore.add(order);

        context.setVariable("totalPrice", totalPrice);
        engine.process("product/payment.html", context, resp.getWriter());
    }
}