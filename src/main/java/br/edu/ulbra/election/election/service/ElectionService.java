package br.edu.ulbra.election.election.service;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.ElectionInput;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.output.v1.ElectionOutput;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import javassist.tools.web.Viewer;


@Service
public class ElectionService {

	private final ElectionRepository electionRepository;
	private final ModelMapper modelMapper;

	private static final String MESSAGE_INVALID_ID = "Invalid id";
	private static final String MESSAGE_ELECTION_NOT_FOUND = "Election not found";

	@Autowired
	public ElectionService(ElectionRepository electionRepository, ModelMapper modelMapper){
		this.electionRepository = electionRepository;
		this.modelMapper = modelMapper;
	}

	public List<ElectionOutput> getAll(){
		Type electionOutputListType = new TypeToken<List<ElectionOutput>>(){}.getType();
		return modelMapper.map(electionRepository.findAll(), electionOutputListType);
	}

	public ElectionOutput getById(Long electionId){
		if (electionId == null){
			throw new GenericOutputException(MESSAGE_INVALID_ID);
		}

		Election election = electionRepository.findById(electionId).orElse(null);
		if (election == null){
			throw new GenericOutputException(MESSAGE_ELECTION_NOT_FOUND);
		}

		return modelMapper.map(election, ElectionOutput.class);
	}

	public List<ElectionOutput> getByYear(Integer year){       
		Type electionOutputListType = new TypeToken<List<ElectionOutput>>(){}.getType();
		return modelMapper.map(electionRepository.findByYear(year), electionOutputListType);
	}

	public ElectionOutput create(ElectionInput electionInput) {
		validateInput(electionInput, false);
		Election election = modelMapper.map(electionInput, Election.class);
		election = electionRepository.save(election);
		return modelMapper.map(election, ElectionOutput.class);
	}

	public ElectionOutput update(Long electionId, ElectionInput electionInput) {
		if (electionId == null){
			throw new GenericOutputException(MESSAGE_INVALID_ID);
		}
		validateInput(electionInput, true);

		Election election = electionRepository.findById(electionId).orElse(null);
		if (election == null){
			throw new GenericOutputException(MESSAGE_ELECTION_NOT_FOUND);
		}

		election.setDescription(electionInput.getDescription());
		election.setStateCode(electionInput.getStateCode());
		election.setYear(electionInput.getYear());

		election = electionRepository.save(election);
		return modelMapper.map(election, ElectionOutput.class);
	}

	public GenericOutput delete(Long electionId) {
		if (electionId == null){
			throw new GenericOutputException(MESSAGE_INVALID_ID);
		}

		Election election = electionRepository.findById(electionId).orElse(null);
		if (election == null){
			throw new GenericOutputException(MESSAGE_ELECTION_NOT_FOUND);
		}

		electionRepository.delete(election);

		return new GenericOutput("Election deleted");
	}


	public Election findByDescription(String description){
		return electionRepository.findByDescription(description);
	}
	public Election findByStateCode(String stateCode){
		return electionRepository.findByStateCode(stateCode);
	}

	private void validateInput(ElectionInput electionInput, boolean isUpdate){
		if (StringUtils.isBlank(electionInput.getDescription())){
			throw new GenericOutputException("Invalid Description");
		}

		if (StringUtils.isBlank(electionInput.getStateCode())){
			throw new GenericOutputException("Invalid State Code");
		}

		if(electionInput.getDescription().length() < 5) {
			throw new GenericOutputException("A Descri��o teve ter no minimo 5 caracteres");
		}

		if(Integer.toString(electionInput.getYear()).length() != 4 ) {
			throw new GenericOutputException("Ano teve ter 4 digitos");
		}
		if(electionInput.getYear() <= 2000 || electionInput.getYear() > 2200) {
			throw new GenericOutputException("Invalid Year");
		}

		String Estados[] = {"AC", "AL", "AM", "AP", "BA","BR", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RO", "RS", "RR", "SC", "SE", "SP", "TO"};
		int status = 0;
		String estado = electionInput.getStateCode();
		for(String x : Estados)
		{
			if(estado .equals(x))
				status = 1;		
		}
		if(status != 1) {
			throw new GenericOutputException("Invalid State Code");
		}
	}

}
