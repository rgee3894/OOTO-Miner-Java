package util.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import model.Column;
import model.Feature;
import model.Response;
import model.Entry;
import preprocessor.Converter;
import preprocessor.PreprocessorIO;
import preprocessor.QuestionAdder;
import util.SwingUpdater;
import view.MainFrame;

public class Preprocessor extends SwingWorker<Void, Integer>{
	private String varDesFilePath, rawFilePath;
	private ArrayList<Entry> newEntries;
	private PreprocessorIO cp;
	private ArrayList<String> header;
	private ArrayList<Feature> newQuestions;
	private MainFrame mainFrame;

	public Preprocessor(String varDesFilePath, String rawFilePath, MainFrame mainFrame){
		this.varDesFilePath = varDesFilePath;
		this.rawFilePath = rawFilePath;
		this.mainFrame = mainFrame;
		this.cp = new PreprocessorIO(this);
	}



	
	
	
	@Override
	protected Void doInBackground() throws Exception {
		// TODO Auto-generated method stub
		Converter conv = new Converter();
		ArrayList<Feature> questionList;
		ArrayList<String> tempStrings;

		header = new ArrayList<>();
		ArrayList<Column> columns = new ArrayList<>();

		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "PROCESS: Reading input files. . .\n");
		questionList = PreprocessorIO.readQuestions(varDesFilePath, "^");
		
		System.out.println("Number of features: " + questionList.size());
		
		
		ArrayList<Entry> oldEntries = cp.readCSV(header, columns, rawFilePath, 1);
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "PROCESS: Converting values in dataset. . .");
		
		System.out.println("Number of old entries: " + oldEntries.size());
		tempStrings = new ArrayList<>();
		
		newQuestions = conv.convertQuestions(columns, questionList, tempStrings);
		System.out.println("Number of new questions: " + newQuestions.size());
		
		for(Feature f : newQuestions)
		{
			System.out.println("Question: " + f.getCode() + " : " + f.getDescription());
			System.out.println("No. of responses: " + f.getGroupedResponseList().size());
			/*
			for(Response r : f.getGroupedResponseList())
			{
				System.out.println("\t"+"Response: " + r.getGroup() + " " + r.getDescription());
			}
			*/
		}
		
		//convert entries
		newEntries = conv.convertEntries(oldEntries, columns, newQuestions);
		
		
		
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "WARNING: The unique values for the following features in the input data set does not match with the values in the variable description file:");
		for(String print: tempStrings){
			SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), print);	
		}
		
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "\nPROCESS: Updating Variable Description. . .");
		//question adder
		QuestionAdder qa = new QuestionAdder();
		tempStrings = qa.addQuestions(questionList, columns);
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "WARNING: The following features were found in the input dataset but not in the variable description file:");
		for(String print: tempStrings){
			SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), print);	
		}
		
		//export csv's
		String exportEntry = "Preprocessed Dataset.csv";
		String exportVar = "Updated-Variables.csv";

		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "\nPROCESS: Exporting Files. . .");
		cp.exportEntries(newEntries, header, exportEntry);
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "DONE: Updated Dataset saved in " + exportEntry + ".\n");

		
		
		
		cp.exportQuestions(newQuestions, exportVar,"^");
		SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus(), "DONE: Updated Variable Description saved in " + exportVar + ".\n");
	
		return null;
	}
	
	public void publishExport(int val){
		publish(new Integer(val));
	}
	
	protected void process(List<Integer> values){
		for(Integer value : values) {
			SwingUpdater.appendJTextAreaText(mainFrame.getTextAreaPreprocessorStatus()
					, "Writing Features (" + value + "/" + newEntries.size() + "). . .");
		}
	}
}