package com.f5.pickupserver.DTO;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class DetailsDTO {
    private String wasteId;
    private String weight;
    private Long pricePreview;
}
