package io.demo.model;

import java.time.OffsetDateTime;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	
	@JsonProperty("role")
	@Column(name = "role")
	@Enumerated(EnumType.ORDINAL)
	private Role role;

	

	public Locale getLocale() {
		if (this.locale==null)
			return null;
		return Locale.forLanguageTag(this.locale);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lasttName;
	}

	public String getEmail() {
		return email;
	}

	/** Username is stored in the base {@code name} field. */
	public String getUsername() {
		return getName();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/** Spring Security authority for this user (single role model). */
	public java.util.List<String> getRolesAsString() {
		if (role == null)
			return java.util.List.of("ROLE_USER");
		return java.util.List.of("ROLE_" + role.name());
	}

	/**
	 * Display name: "first last", falling back to the email if no name is set.
	 */
	public String getDisplayname() {
		StringBuilder sb = new StringBuilder();
		if (firstName != null)
			sb.append(firstName);
		if (lasttName != null) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(lasttName);
		}
		if (sb.length() == 0 && getName() != null)
			sb.append(getName());
		return sb.toString();
	}

}
