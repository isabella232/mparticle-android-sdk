package com.mparticle.commerce;

import com.mparticle.MParticle;
import com.mparticle.mock.MockContext;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CartTest {

    private static Cart cart;

    @BeforeClass
    public static void setupAll() {
        MParticle mockMp = Mockito.mock(MParticle.class);
        Mockito.when(mockMp.getEnvironment()).thenReturn(MParticle.Environment.Development);
        MParticle.setInstance(mockMp);
        cart = Cart.getInstance(new MockContext());
    }

    @Before
    public void setupEachTest() {
        cart.clear();
        Cart.setProductEqualityComparator(null);
    }

    @Test
    public void testSetProductEqualityComparator() throws Exception {
        Product product = new Product.Builder("matching name", "sku").build();
        Product product2 = new Product.Builder("matching name", "sku").build();
        cart.add(product);
        assertEquals(1, cart.products().size());
        cart.remove(product2);
        assertEquals(1, cart.products().size());
        Cart.setProductEqualityComparator(new Product.EqualityComparator() {
            @Override
            public boolean equals(Product product1, Product product2) {
                return product1.getName().equalsIgnoreCase(product2.getName());
            }
        });
        cart.remove(product2);
        assertEquals(0, cart.products().size());
    }

    @Test
    public void testAdd() throws Exception {
        Product nullProduct = null;
        cart.add(nullProduct);
        Product product = new Product.Builder("name 1", "sku").build();
        Product product2 = new Product.Builder("name 2", "sku").build();
        cart.add(product, product2, nullProduct);
        assertEquals(2, cart.products().size());
        cart.add(product, product2);
        assertEquals(2, (int) cart.products().get(0).getQuantity());
        assertEquals(2, (int) cart.products().get(1).getQuantity());
    }

    @Test
    public void testRemoveWithProduct() throws Exception {
        testAdd();
        Cart.setProductEqualityComparator(new Product.EqualityComparator() {
            @Override
            public boolean equals(Product product1, Product product2) {
                return product1.getName().equalsIgnoreCase(product2.getName());
            }
        });
        Product nullProduct = null;
        Product product = new Product.Builder("name 1", "sku 1").build();
        Product product2 = new Product.Builder("name 2", "sku 2").build();
        cart.remove(nullProduct);
        cart.remove(product, product2, nullProduct);
        assertEquals(1, (int) cart.getProduct("name 1").getQuantity());
        assertEquals(1, (int) cart.getProduct("name 2").getQuantity());
        cart.remove(product, product2, nullProduct);
        assertNull(cart.getProduct("name 1"));
        assertNull(cart.getProduct("name 2"));
    }

    @Test
    public void testRemoveWithIndex() throws Exception {
        testAdd();
        cart.remove(0);
        assertEquals(1, cart.products().size());
        cart.remove(6);
    }

    @Test
    public void testCheckout() throws Exception {
        cart.checkout();
        cart.checkout(-1, null);
        cart.checkout(0, "");
    }


    @Test
    public void testPurchase() throws Exception {
        testAdd();
        try {
            cart.purchase(null);
        }catch (IllegalStateException stateException){

        }
        assertEquals(2, cart.products().size());
        try {
            cart.purchase(new TransactionAttributes());
        }catch (IllegalStateException stateException){

        }
        assertEquals(2, cart.products().size());
        cart.purchase(new TransactionAttributes("trans id"));
        assertEquals(2, cart.products().size());
        cart.purchase(new TransactionAttributes("trans id"), true);
        assertEquals(0, cart.products().size());
    }

    @Test
    public void testRefund() throws Exception {
        try {
            cart.refund(null);
        }catch (IllegalStateException stateexception){

        }
        try {
            cart.refund(new TransactionAttributes());
        }catch (IllegalStateException illegalstateexception){

        }
        cart.refund(new TransactionAttributes("trans id"));
    }

    @Test
    public void testLoadFromString() throws Exception {
        testAdd();
        assertEquals(2, cart.products().size());
        String string = cart.toString();
        cart.clear();
        assertEquals(0, cart.products().size());
        cart.loadFromString(string);
        assertEquals(2, cart.products().size());
    }

    @Test
    public void testToString() throws Exception {
        JSONObject json = new JSONObject(cart.toString());
        testAdd();
        JSONObject json2 = new JSONObject(cart.toString());
        assertEquals(2, json2.getJSONArray("pl").length());
    }

    @Test
    public void testClear() throws Exception {
        cart.clear().clear().clear();
        testAdd();
        cart.clear();
        assertEquals(0, cart.products().size());
    }

    @Test
    public void testGetProduct() throws Exception {
        testAdd();
        assertNotNull(cart.getProduct("name 1"));
    }

    @Test
    public void testProducts() throws Exception {
        Exception e = null;
        try {
            cart.products().add(new Product.Builder("name","sku").build());
        } catch (Exception uoe) {
            e = uoe;
        }
        assertTrue(e instanceof UnsupportedOperationException);
    }
}