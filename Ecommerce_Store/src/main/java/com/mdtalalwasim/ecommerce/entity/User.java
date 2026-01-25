package com.mdtalalwasim.ecommerce.entity;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String mobile;
	
	private String email;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String pinCode;
	
	private String password;
	
	private String profileImage;
	
	private String role;

    @Column(nullable = false)
	private Boolean isEnable = true;
	
	//implement user account lock for wrong password
    @Column(nullable = false)
	private Boolean accountStatusNonLocked = true;

    @Column(nullable = false)
	private Integer accountfailedAttemptCount=0;
	
	private Date accountLockTime;
	
	private String resetTokens;
	
	@CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


	
	
}
