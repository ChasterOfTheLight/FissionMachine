package com.devil.fission.machine.auth.api.dto;

import com.devil.fission.machine.auth.api.enums.AccessSourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * sign verify dto.
 *
 * @author devil
 * @date Created in 2022/12/28 14:52
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifySignDto implements Serializable {
    
    private static final long serialVersionUID = -2678017495501156095L;
    
    /**
     * api source.
     */
    private AccessSourceEnum accessSource;
    
}
