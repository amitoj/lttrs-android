package rs.ltt.android.entity;

import com.google.common.base.Objects;

public class EmailAddress {
    public EmailAddressType type;
    public String email;
    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return type == that.type &&
                Objects.equal(email, that.email) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, email, name);
    }
}
