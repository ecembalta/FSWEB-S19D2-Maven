package com.workintech.s18d4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workintech.s18d4.controller.AccountController;
import com.workintech.s18d4.controller.CustomerController;
import com.workintech.s18d4.dto.CustomerResponse;
import com.workintech.s18d4.entity.Account;
import com.workintech.s18d4.entity.Customer;
import com.workintech.s18d4.service.AccountService;
import com.workintech.s18d4.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = { ControllerAndPropertiesTest.class, AccountController.class, CustomerController.class })
@ExtendWith(ResultAnalyzer2.class)
class ControllerAndPropertiesTest {

    @Autowired
    private Environment env;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CustomerService customerService;

    private Account sampleAccountForAccountControllerTest;
    private Customer sampleCustomerForAccountControllerTest;

    private Customer sampleCustomerForCustomerControllerTest;

    @BeforeEach
    void setUp() {
        sampleCustomerForAccountControllerTest = new Customer();
        sampleCustomerForAccountControllerTest.setId(1L);
        sampleCustomerForAccountControllerTest.setEmail("customer@example.com");
        sampleCustomerForAccountControllerTest.setSalary(5000.00);

        sampleAccountForAccountControllerTest = new Account();
        sampleAccountForAccountControllerTest.setId(1L);
        sampleAccountForAccountControllerTest.setAccountName("Savings Account");
        sampleAccountForAccountControllerTest.setMoneyAmount(1000.00);
        sampleAccountForAccountControllerTest.setCustomer(sampleCustomerForAccountControllerTest);

        // Use an ArrayList to allow modifications
        List<Account> modifiableAccountsList = new ArrayList<>();
        modifiableAccountsList.add(sampleAccountForAccountControllerTest);
        sampleCustomerForAccountControllerTest.setAccounts(modifiableAccountsList);

        sampleCustomerForCustomerControllerTest = new Customer();
        sampleCustomerForCustomerControllerTest.setId(1L);
        sampleCustomerForCustomerControllerTest.setEmail("customer@example.com");
        sampleCustomerForCustomerControllerTest.setSalary(5000.00);
    }

    @Test
    @DisplayName("application properties istenilenler eklendi mi?")
    void serverPortIsSetTo8585() {

        String serverPort = env.getProperty("server.port");
        assertThat(serverPort).isEqualTo("8080");

        String datasourceUrl = env.getProperty("spring.datasource.url");
        assertNotNull(datasourceUrl);

        String datasourceUsername = env.getProperty("spring.datasource.username");
        assertNotNull(datasourceUsername);

        String datasourcePassword = env.getProperty("spring.datasource.password");
        assertNotNull(datasourcePassword);

        String hibernateDdlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");
        assertNotNull(hibernateDdlAuto);

    }

    @Test
    @DisplayName("AccountController::findAll")
    void testFindAllAccount() throws Exception {
        when(accountService.findAll()).thenReturn(List.of(sampleAccountForAccountControllerTest));

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) sampleAccountForAccountControllerTest.getId())))
                .andExpect(jsonPath("$[0].accountName", is(sampleAccountForAccountControllerTest.getAccountName())));

        verify(accountService).findAll();
    }

    @Test
    @DisplayName("AccountController::find")
    void testFindAccount() throws Exception {
        when(accountService.find(sampleAccountForAccountControllerTest.getId()))
                .thenReturn(sampleAccountForAccountControllerTest);

        mockMvc.perform(get("/account/{id}", sampleAccountForAccountControllerTest.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) sampleAccountForAccountControllerTest.getId())))
                .andExpect(jsonPath("$.accountName", is(sampleAccountForAccountControllerTest.getAccountName())));

        verify(accountService).find(sampleAccountForAccountControllerTest.getId());
    }

    @Test
    @DisplayName("AccountController::save")
    void testSaveAccount() throws Exception {
        when(customerService.find(sampleCustomerForAccountControllerTest.getId()))
                .thenReturn(sampleCustomerForAccountControllerTest);
        when(accountService.save(any())).thenReturn(sampleAccountForAccountControllerTest);

        mockMvc.perform(post("/account/{customerId}", sampleCustomerForAccountControllerTest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleAccountForAccountControllerTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) sampleAccountForAccountControllerTest.getId())))
                .andExpect(jsonPath("$.accountName", is(sampleAccountForAccountControllerTest.getAccountName())));

        verify(customerService).find(sampleCustomerForAccountControllerTest.getId());
        verify(accountService).save(any());
    }

    @Test
    @DisplayName("AccountController::update")
    void testUpdateAccount() throws Exception {
        long customerId = sampleCustomerForAccountControllerTest.getId();
        Account updatedAccount = new Account();
        updatedAccount.setId(sampleAccountForAccountControllerTest.getId());
        updatedAccount.setAccountName("Updated Account");
        updatedAccount.setMoneyAmount(2000.00);
        updatedAccount.setCustomer(sampleCustomerForAccountControllerTest);

        // Ensure the customer is associated with the account to be updated
        List<Account> accounts = new ArrayList<>();
        accounts.add(sampleAccountForAccountControllerTest);
        sampleCustomerForAccountControllerTest.setAccounts(accounts);

        when(customerService.find(customerId)).thenReturn(sampleCustomerForAccountControllerTest);
        when(accountService.find(sampleAccountForAccountControllerTest.getId()))
                .thenReturn(sampleAccountForAccountControllerTest);
        when(accountService.save(any())).thenReturn(updatedAccount);

        mockMvc.perform(put("/account/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) updatedAccount.getId())))
                .andExpect(jsonPath("$.accountName", is(updatedAccount.getAccountName())))
                .andExpect(jsonPath("$.moneyAmount", is(updatedAccount.getMoneyAmount())));

        verify(customerService).find(customerId);
        given(customerService.save(any())).willReturn(sampleCustomerForAccountControllerTest);
    }

    @Test
    @DisplayName("AccountController::remove")
    void testRemoveAccount() throws Exception {
        when(accountService.find(sampleAccountForAccountControllerTest.getId()))
                .thenReturn(sampleAccountForAccountControllerTest);
        when(accountService.delete(sampleAccountForAccountControllerTest.getId()))
                .thenReturn(sampleAccountForAccountControllerTest);

        mockMvc.perform(delete("/account/{id}", sampleAccountForAccountControllerTest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) sampleAccountForAccountControllerTest.getId())))
                .andExpect(jsonPath("$.accountName", is(sampleAccountForAccountControllerTest.getAccountName())))
                .andExpect(jsonPath("$.moneyAmount", is(sampleAccountForAccountControllerTest.getMoneyAmount())));

        verify(accountService).find(sampleAccountForAccountControllerTest.getId());
        verify(accountService).delete(sampleAccountForAccountControllerTest.getId());
    }

    @Test
    @DisplayName("CustomerController::saveCustomer")
    void testSaveCustomer() throws Exception {
        given(customerService.save(any())).willReturn(sampleCustomerForCustomerControllerTest);

        CustomerResponse expectedResponse = new CustomerResponse(sampleCustomerForCustomerControllerTest.getId(),
                sampleCustomerForCustomerControllerTest.getEmail(),
                sampleCustomerForCustomerControllerTest.getSalary());

        mockMvc.perform(post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCustomerForCustomerControllerTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) expectedResponse.id())))
                .andExpect(jsonPath("$.email", is(expectedResponse.email())))
                .andExpect(jsonPath("$.salary", is(expectedResponse.salary())));

        verify(customerService).save(any());
    }
}
