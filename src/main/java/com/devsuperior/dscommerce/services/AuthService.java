package com.devsuperior.dscommerce.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;

@Service
public class AuthService {

	@Autowired
	private UserService userService;
	
	public void validateSelfOrAdmin(long userId) {
		User me = userService.authenticated();//Peguei esse utilizador autenticado, depois na linha abaixo se esse user não tiver ROLE_ADMIN e o ID do utilizador "me" não for igual ao ID que enviei com o parametro("userId") do metdodo validateSelfOrAdmin supra, se isso for verdade quer dizer que ele nem é ADMIN nem é o proprio utilizador aqui do argumento que chegou no metodo
		if (!me.hasRole("ROLE_ADMIN") && !me.getId().equals(userId)) {
			throw new ForbiddenException("Access denied"); //mediante a explicação em cima será lançada uma excepção
		}
	}
}
