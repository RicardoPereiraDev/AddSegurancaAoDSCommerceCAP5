package com.devsuperior.dscommerce.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.Role;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository repository;
	
	@Override //temos que implementar o metodo loadUserByUsername recebendo o username
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		//Faz a consulta no banco, se vier vazio lança uma excepção
		List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
		if (result.size() == 0) {
			throw new UsernameNotFoundException("Email not found");
		}

		//Caso contrario, ou seja, se foi encontrado faz a instancia do user e retorna o user no final
		User user = new User();
		user.setEmail(result.get(0).getUsername());
		user.setPassword(result.get(0).getPassword());
		for (UserDetailsProjection projection : result) {
			user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
		}
		
		return user;
	}

	/*Fizemos aqui um metodo auxiliar que retorna um user que está logado */
	protected User authenticated() {
		try { // é para se não estiver logado/autenticado e capturar uma excepção
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //vai pegar dentro do contexto da autenticação um user se tiver autenticado
			Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
			String username = jwtPrincipal.getClaim("username"); //o email do user que está no token vai cair na variavel username e eu depois posso utilizar dentro do metodo abaixo findByEmail
			return repository.findByEmail(username).get();
		}
		catch (Exception e) {
			throw new UsernameNotFoundException("Invalid user");
		}
	}
	
	@Transactional(readOnly = true) //Para pegar o user que está logado com base no token que está guardado no contexto do spring security , que é o codigo do metodo authenticated()
	public UserDTO getMe() {
		User entity = authenticated();//peguei o user do banco
		return new UserDTO(entity);// Agora vou retornar esse user convertido para UserDTO
	}
}
