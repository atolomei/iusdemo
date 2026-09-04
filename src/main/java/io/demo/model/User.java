package io.demo.model;

import java.time.OffsetDateTime;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(Include.NON_NULL)
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends DemoDBObject {

	public static String normalizeEmail(String e) {
		if (e == null)
			return null;
		return e.toLowerCase().trim();
	}

	public static String normalizePhone(String phone) {
		if (phone == null)
			return null;
		return phone.replaceAll("[^0-9+]", "");
	}

	@JsonProperty("firstName")
	@Column(name = "firstName")
	private String firstName;

	@JsonProperty("lasttName")
	@Column(name = "lastName")
	private String lasttName;

	@JsonProperty("email")
	@Column(name = "email")
	private String email;

	@JsonProperty("phone")
	@Column(name = "phone")
	private String phone;

	@JsonProperty("zoneId")
	@Column(name = "zoneId")
	private String zoneId;

	

	@JsonProperty("locale")
	@Column(name = "locale")
	private String locale;
	
	@JsonProperty("password")
	@Column(name = "password")
	private String password;

	@JsonProperty("showWelcome")
	@Column(name = "showWelcome")
	private boolean showWelcome;

	@JsonProperty("initialsignindone")
	@Column(name = "initialsignindone")
	private boolean initialsignindone;

	@JsonProperty("welcomeEmailSent")
	@Column(name = "welcomeEmailSent")
	private OffsetDateTime welcomeEmailSent;



	public Locale getLocale() {
		if (this.locale==null)
			return null;
		return Locale.forLanguageTag(this.locale);
	}
	
}
