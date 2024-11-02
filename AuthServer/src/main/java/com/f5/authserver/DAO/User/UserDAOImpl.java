package com.f5.authserver.DAO.User;

import com.f5.authserver.DTO.RegisterDTO;
import com.f5.authserver.DTO.UserDTO;
import com.f5.authserver.DTO.UserDetailDTO;
import com.f5.authserver.Entity.DormantEntity;
import com.f5.authserver.Entity.UserEntity;
import com.f5.authserver.Repository.DormantRepository;
import com.f5.authserver.Repository.UserRepository;
import com.f5.authserver.Service.Communication.AccountCommunicationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountCommunicationService accountCommunicationService;
    private final DormantRepository dormantRepository;

    private UserDTO entityToDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .username(userEntity.getUsername())
                .password(passwordEncoder.encode(userEntity.getPassword()))
                .build();
    }

    @Override
    public UserDTO save(RegisterDTO registerDTO) {
        try {
            UserEntity userEntity = UserEntity.builder()
                    .username(registerDTO.getUsername())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .build();
            userRepository.save(userEntity);
            UserDetailDTO userDetailDTO = UserDetailDTO.builder()
                    .id(userEntity.getId())
                    .name(registerDTO.getName())
                    .postalCode(registerDTO.getPostalCode())
                    .roadNameAddress(registerDTO.getRoadNameAddress())
                    .detailedAddress(registerDTO.getDetailedAddress())
                    .email(registerDTO.getEmail())
                    .phoneNumber(registerDTO.getPhoneNumber())
                    .build();
            accountCommunicationService.registerAccount(userDetailDTO);
            return entityToDTO(userEntity);
        } catch (Exception e) {
            throw new IllegalStateException("잘못된 형식의 요청.", e);
        }
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username) || dormantRepository.existsByUsername(username);
    }

    @Override
    public UserDTO getByUsername(String username) {
        try {
            UserEntity user = userRepository.getByUsername(username);
            return UserDTO.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .build();
        } catch (Exception e){
            throw new IllegalStateException("해당 사용자 이름을 찾을 수 없습니다.", e);
        }
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);  // Optional로 엔티티 반환
    }


    @Override
    public void moveToDormantAccount(UserDTO userDTO) {
        try {
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                UserEntity user = userRepository.getByUsername(userDTO.getUsername());
                DormantEntity dormant = DormantEntity.builder()
                        .Id(user.getId())
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .dormantDate(LocalDate.now())
                        .build();
                dormantRepository.save(dormant);
                accountCommunicationService.dormantAccount(dormant.getId()); // 중첩 예외 제거
                userRepository.delete(user); // 실제 삭제 로직 확인 필요
            } else {
                throw new IllegalArgumentException("해당 아이디가 없음");
            }
        } catch (Exception e) {
            throw new IllegalStateException("삭제에 실패 하였습니다.", e);
        }
    }

    @Override
    public void removeDormantAccount() {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<DormantEntity> expiredDormantAccounts = dormantRepository.findByDormantDateBefore(threeMonthsAgo);

        for (DormantEntity dormant : expiredDormantAccounts) {
            System.out.println(LocalDate.now()+ " " + dormant.getUsername() + " 계정이 삭제됨");
            dormantRepository.delete(dormant);
        }
    }

    @Override
    public Long getId(String username){
        return userRepository.getByUsername(username).getId();
    }
}
