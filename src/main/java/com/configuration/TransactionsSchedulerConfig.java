package com.configuration;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.dto.Transaction;
import com.dto.TransactionOutputs;
import com.items.TransactionItemProcessor;
import com.items.TransactionItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;

@Component
@Configuration
@EnableBatchProcessing
@EnableScheduling
@PropertySource("classpath:application.properties")
public class TransactionsSchedulerConfig {
	
	Logger LOGGER  = LoggerFactory.getLogger(TransactionsSchedulerConfig.class);
	
	/*Retrieving the value of Environment table*/
	@Value("${environment}")
	private String environment;	
	
	@Autowired
    private SimpleJobLauncher jobLauncher;
	
	@Autowired
	private JobBuilderFactory job;
	
	@Autowired
	private StepBuilderFactory step;
	
	@Autowired
	private CheckContentHashOfInputFile checkContentHashFile;
	
	@Bean
    public ResourcelessTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	
	@Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(ResourcelessTransactionManager txManager) throws Exception {     
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(txManager);       
        factory.afterPropertiesSet();        
        return factory;
    }
	
	@Bean
    public JobRepository jobRepository(MapJobRepositoryFactoryBean factory) throws Exception {
        return factory.getObject();
    }
	
	@Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
	
	/*Customer transactions are received twice daily*/
	@Scheduled(cron = "0 0 6,21 * * *")
    public void perform() throws Exception {
		LOGGER.info("Job Started at :" + new Date());
       JobParameters param = new JobParametersBuilder().addString("JobID",
                String.valueOf(System.currentTimeMillis())).toJobParameters();

        JobExecution execution = jobLauncher.run(processTransactionJob(), param);
        LOGGER.info("Job finished with status :" + execution.getStatus());
    }
	
	/*Defining the job and its steps*/
	@Bean
	public Job processTransactionJob()
	{
		return job.get("processTransactionJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1()).end().build();
	}
	
	@Bean
	public Step step1()
	{
		return step.get("step1").<Transaction, TransactionOutputs> chunk(10)
        .reader(reader()).processor(processor()).writer(writer())
        .build();
	}
	
	
	/*Configuring the ItemReader*/
	@Bean
	public FlatFileItemReader<Transaction> reader()
	{
		/*Fetching the file from the location matching the 
		 * environment set at the application.properties*/		
		String getFileName = "";
		String finalFileName = "";
		//String processingFiles = System.getenv(environment) +"\\pending";
		
		/*File is available inside pending folder inside 
		 * the environment variable*/
		String processingFiles = environment +"\\pending";
		File dir = new File(processingFiles);
		File[] files = dir.listFiles();
		if(files.length != 0)
		{
			/*Retrieving the first file from the environemt folder*/
			 getFileName = files[0].getName();
		}
		
		/*Checking if the file name is correct*/
		if(getFileName!= null && getFileName.startsWith("finance_customer_transactions") &&
				getFileName.endsWith(".csv"))
		{
			finalFileName = getFileName;
		}
		else
			finalFileName = "sample-data.csv";
		
		/*Sample.csv is just put in classpath to show what that file currently has.
		 * Actually it is  being kept inside pending folder and to run when
		 * inance_customer_transactions fails for some reason.*/
		
		/*Checking the OS where the program resides,
		 * because FileSeperator varies for different OS*/
		String operating_system = System.getProperty("os.name");	
		if(operating_system.startsWith("Windows"))
		{
			processingFiles = processingFiles.replace("\\", "//");
		    processingFiles = processingFiles + "//" + 	finalFileName;
		}
		else
			processingFiles = processingFiles + File.separator + finalFileName;
		
		try {
			
			/*This try catch is used to determine the file has been executed 
			 * just once :
			 * Step 1 : checkContentHashFile.getContent - generates a checksum
			 * for the file. */
			checkContentHashFile.getContent(processingFiles,finalFileName);
			if(operating_system.startsWith("Windows"))
			{
				String hashFile = environment + File.separator +"hashOfFiles";
				hashFile = hashFile.replace("\\", "//");
				
				/*Step 2 : Storing the hashKey and filename into a csv for future
				 * reference.*/
				checkContentHashFile.SetFile(hashFile);
			}
			
			/*Reading or Lookup the csv file where hashKey of file is stored,
			 * and checking if it matches with that of hashKey of the new file,
			 * to avoid duplicate processing of file.*/
			String key = checkContentHashFile. ReadCSV();
			
			if(key== null && operating_system.startsWith("Windows"))
				processingFiles = environment + "//hashOfFiles//"  + "sample-data.csv";
			
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
			
		/*Reading the input file line by line*/
		FlatFileItemReader<Transaction> reader = new FlatFileItemReader<>();
		
		/*Skipping the first line as it has headings*/
		reader.setLinesToSkip(1);
		
		/*Maximum number of records that can be processed*/
		reader.setMaxItemCount(500000);
		reader.setResource(new FileSystemResource(processingFiles));
		LOGGER.info("FileName and Path :" +processingFiles);
        reader.setLineMapper(new DefaultLineMapper<Transaction>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
            	
            	/*These are names of fields inside csv*/
                setNames(new String[] { "Customer_Account", "Transaction_Amount" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {{
                setTargetType(Transaction.class);
            }});
        }});
        return reader;
	}
	
	/*Configuring the ItemProcessor*/
	@Bean
	ItemProcessor<? super Transaction, ? extends TransactionOutputs> processor() {
		return new TransactionItemProcessor();
	}
	
	/*Configuring the ItemWriter*/
	@Bean
	ItemWriter<TransactionOutputs> writer() {
		String processedFiles = environment + File.separator + "processed";
		processedFiles = processedFiles.replace("\\", "//");
		return new TransactionItemWriter(processedFiles);
	}
	
}
