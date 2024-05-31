package accounts.web;

import accounts.AccountManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.money.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rewards.internal.account.Account;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AccountController.class) // includes @ExtendWith(SpringExtension.class)
public class AccountControllerBootTests {

	 @Autowired
	 MockMvc mockMvc;

	 @MockBean
	 AccountManager accountManager;

	@Test
	public void accountDetails() throws Exception {

		given(accountManager.getAccount(0L))
				.willReturn(new Account("1234567890", "John Doe"));

		mockMvc.perform(get("/accounts/0"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(jsonPath("name").value("John Doe"))
			   .andExpect(jsonPath("number").value("1234567890"));

		verify(accountManager).getAccount(0L);
	}

	@Test
	public void accountDetailsFail() throws Exception {

		given(accountManager.getAccount(any(Long.class)))
				.willThrow(new IllegalArgumentException("No such account with id " + 0L));

		mockMvc.perform(get("/accounts/9999"))
				.andExpect(status().isNotFound());
		verify(accountManager).getAccount(any(Long.class));
	}

	@Test
	public void createAccount() throws Exception {

		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.setEntityId(21L);

		given(accountManager.save(any(Account.class)))
				.willReturn(testAccount);

		mockMvc.perform(post("/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asJsonString(testAccount)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/accounts/21"));

		verify(accountManager).save(any(Account.class));
	}

	@Test
	public void getAllAccounts() throws Exception {
		Account testAccount = new Account("1234512345", "Mary Jones");
		given(accountManager.getAllAccounts()).willReturn(Collections.singletonList(testAccount));


		mockMvc.perform(get("/accounts"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$[0].name").value("Mary Jones"))
				.andExpect(jsonPath("$[0].number").value("1234512345"));

		verify(accountManager).getAllAccounts();
	}

	@Test
	public void getBeneficiary() throws Exception {
		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.addBeneficiary("David", Percentage.valueOf("0.3"));


		given(accountManager.getAccount(1L))
				.willReturn(testAccount);

		mockMvc.perform(get("/accounts/{accountId}/beneficiaries/{beneficiaryName}", 1, "David"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("David"))
				.andExpect(jsonPath("$.allocationPercentage").value("0.3"));

		verify(accountManager).getAccount(1L);
	}

	@Test
	public void getBeneficiaryNotFound() throws Exception {
		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.addBeneficiary("David", Percentage.valueOf("0.3"));


		given(accountManager.getAccount(1L))
				.willReturn(testAccount);

		mockMvc.perform(get("/accounts/{accountId}/beneficiaries/{beneficiaryName}", 1, "Jago"))
				.andExpect(status().isNotFound());

		verify(accountManager).getAccount(1L);
	}

	@Test
	public void addNewBeneficiary() throws Exception {
		mockMvc.perform(post("/accounts/{accountId}/beneficiaries", 1)
						.contentType("application/json")
						.content("David"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/accounts/1/beneficiaries/David"));

		verify(accountManager).addBeneficiary(1L, "David");
	}

	@Test
	public void deleteBeneficiary() throws Exception {
		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.addBeneficiary("David", Percentage.valueOf("0.3"));


		when(accountManager.getAccount(1L)).thenReturn(testAccount);

		mockMvc.perform(delete("/accounts/{accountId}/beneficiaries/{beneficiaryName}", 1L, "David"))
				.andExpect(status().isNoContent());

		verify(accountManager).removeBeneficiary(eq(1L), eq("David"), any());
	}

	@Test
	public void deleteNonExistentBeneficiary() throws Exception {
		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.addBeneficiary("David", Percentage.valueOf("0.3"));

		when(accountManager.getAccount(1L)).thenReturn(null);

		mockMvc.perform(delete("/accounts/{accountId}/beneficiaries/{beneficiaryName}", "1", "David"))
				.andExpect(status().isNotFound());
	}

    // Utility class for converting an object into JSON string
	protected static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
