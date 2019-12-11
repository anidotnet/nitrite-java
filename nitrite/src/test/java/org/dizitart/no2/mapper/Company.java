package org.dizitart.no2.mapper;

import lombok.Data;
import org.dizitart.no2.collection.Document;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class Company {
    private String name;
    private Long id;
    private CompanyId companyId;

    @Data
    public static class CompanyId implements Comparable<CompanyId>, Serializable {
        private Long idValue;

        public CompanyId(long value) {
            this.idValue = value;
        }

        @Override
        public int compareTo(CompanyId other) {
            return idValue.compareTo(other.idValue);
        }

        public static TypeConverter<CompanyId> getConverter() {
            return new TypeConverter<>(
                CompanyId.class,
                (source, mapper) -> Document.createDocument("idValue", source.idValue),
                (source, mapper) -> new CompanyId(source.get("idValue", Long.class))
            );
        }
    }

    public static TypeConverter<Company> getConverter() {
        return new TypeConverter<>(
            Company.class,
            (source, mapper) -> Document.createDocument("id", source.id)
                .put("name", source.name)
                .put("companyId", mapper.convert(source.companyId, Document.class)),
            (source, mapper) -> {
                Company company = new Company();
                company.name = source.get("name", String.class);
                company.id = source.get("id", Long.class);
                company.companyId = source.get("companyId", CompanyId.class);
                return company;
            }
        );
    }
}
