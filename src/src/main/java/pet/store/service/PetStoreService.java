package pet.store.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreCustomer;
import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {
	
	@Autowired
	private  PetStoreDao  petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;
	
    
	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		Long petStoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petStoreId);
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
	}

	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreId(petStoreData.getPetStoreId());
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
	}

	private PetStore findOrCreatePetStore(Long petStoreId) {
		if(Objects.isNull(petStoreId)) {
			return new PetStore();
		} else {
		return findPetStoreById(petStoreId);
		}
	}

	private PetStore findPetStoreById(Long petStoreId) {
		return petStoreDao.findById(petStoreId)
				.orElseThrow(() -> new NoSuchElementException
		("Pet store with Id = " + petStoreId + " was not found"));
	}

	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
		 PetStore petStore = findPetStoreById(petStoreId);
		 Long employeeId = petStoreEmployee.getEmployeeId();
		 Employee employee = findOrCreateEmployee(petStoreId, employeeId);
		 copyEmployeeFields(employee, petStoreEmployee);
		 employee.setPetStore(petStore);
		 petStore.getEmployees().add(employee);
		 Employee dbemployee = employeeDao.save(employee);
		 return new PetStoreEmployee(dbemployee);
	}

	
	@Transactional(readOnly = false)
	public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
		 PetStore petStore = findPetStoreById(petStoreId);
		 Long customerId = petStoreCustomer.getCustomerId();
		 Customer customer = findOrCreateCustomer(petStoreId, customerId);
		 copyCustomerFields(customer, petStoreCustomer);
		 customer.getPetStores().add(petStore);
		 petStore.getCustomers().add(customer);
		 Customer dbCustomer = customerDao.save(customer);
		 
		return new PetStoreCustomer(dbCustomer);
	}

	
	
	private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeId(petStoreEmployee.getEmployeeId());
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployesJobTitle(petStoreEmployee.getEmployesJobTitle());
		employee.setEmployesPhone(petStoreEmployee.getEmployesPhone());
	}
	
	
	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
		customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
		customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
		customer.setCustomerId(petStoreCustomer.getCustomerId());
		customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
	}

	private Employee findOrCreateEmployee(Long petStoreId, Long employeeId) {
		
		if(Objects.isNull(employeeId)) {
			return new Employee();
		} else {
		return findEmployeeById(petStoreId,employeeId);
		}
	}

	private Employee findEmployeeById(Long petStoreId, Long employeeId) {
			Employee employee = employeeDao.getReferenceById(employeeId);                
					//orElseThrow(() -> new NoSuchElementException("Employee with Id = " + employeeId + " was not found"));
			
			if (employee.getPetStore().getPetStoreId() != petStoreId) {
				throw new IllegalArgumentException("Employee with Id " + employeeId + " doesn't work in the Pet store with Id  " + petStoreId );
				
			}
		return employee;
	}

	
	private Customer findOrCreateCustomer(Long petStoreId, Long customerId) {
		if(Objects.isNull(customerId)) {
			return new Customer();
		} else {
		return findCustomerById(petStoreId,customerId);
		}
	}

	private Customer findCustomerById(Long petStoreId, Long customerId) {
		
		Customer customer = customerDao.getReferenceById(customerId);
		boolean found = false;
		for(PetStore petStore : customer.getPetStores()) {
			if (petStore.getPetStoreId() == petStoreId) {
				found = true;
				break;
			}
		}
		if(!found) {
			throw new IllegalArgumentException("The customer with Id="+customerId+" is not a member of the pet store with ID="+petStoreId);
		}
		
		return customer;
	}

	@Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStore() {
		List<PetStore> petStores = petStoreDao.findAll();
		List<PetStoreData> result = new LinkedList<>();
		for(PetStore petStore : petStores) {

			PetStoreData psd = new PetStoreData(petStore);
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			result.add(psd);
		}
		return result;
	}
    
	@Transactional(readOnly = true)
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		
		return new PetStoreData(findPetStoreById(petStoreId));
	}

	@Transactional(readOnly = false)
	public void deletePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		petStoreDao.delete(petStore);
	}
	
	
	
	
// class end
}
