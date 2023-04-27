package assignment3;



//Author: Andrew Johnson
//Date/Version: January 27, 2023
/*
 * The purpose of this program is to put students into an ArrayList.
 * Once the list is populated with students then the user can modify
 * the grade of any of the students.
 * This program should not crash regardless of what the user types in
 * since all errors and Exceptions are being caught by try-catches.
 */

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class StudentList {

	public static void main(String[] args) {
		ArrayList<Student> studentList = new ArrayList<Student>();
		Scanner input = new Scanner(System.in);
		boolean finished = false;
		int menuChoice = 0;

		while (!finished) {

			menuChoice = menu(input);

			if (menuChoice == 1) {
				addStudent(studentList, input);
			} else if (menuChoice == 2 && studentList.size() > 0) {
				changeGrade(studentList, input);
			}else if(menuChoice == 2 && studentList.size() == 0) {
				System.out.println("You must add at least one student using option 1 before you can select option 2");
			} else if (menuChoice == 3) {
				System.out.println("Thank you, Goodbye!");
				finished = true;
				input.close();
			}else {
				System.out.println("Only numeric choices 1, 2, or 3");
			}
		}
	}

	public static int menu(Scanner input) {
		boolean badInput = true;
		int choice = 0;
		do {
			try {
				System.out.println("Main menu");
				System.out.println();
				System.out.println("Select one of the following options");
				System.out.println("1. Add a student");
				System.out.println("2. Edit student grades");
				System.out.println("3. Exit");
				choice = input.nextInt();
				badInput = false;
			} catch (InputMismatchException e) {
				System.out.println("You may only select from the options presented.");
				input.nextLine();
			}catch(Exception e) {
				System.out.println(e);
				input.nextLine();
			}
		} while (badInput);
		return choice;
	}

	private static void addStudent(ArrayList<Student> list, Scanner input) {
		String name = input.nextLine();
		double grade = 0;
		boolean badInput = true;
		System.out.println("Please enter the students name:");
		name = input.nextLine();
		do {
			try {
				System.out.println("Please enter a grade for " + name + ":");
				grade = input.nextDouble();
				if(0 <= grade && grade <= 100) {
					badInput = false;
				}else {
					System.out.println("You must enter a grade as a number between 0 and 100");
				}
			} catch (InputMismatchException e) {
				System.out.println("You must enter a grade as a number between 0 and 100");
				input.nextLine();
			}catch(Exception e) {
				System.out.println(e);
				input.nextLine();
			}
		} while (badInput);
		list.add(new Student(name, grade));
	}

	private static void changeGrade(ArrayList<Student> list, Scanner input) {
		boolean badInput = true;
		int studentChoice = 0;
		do {
			
			try {
				System.out.println("What student would you like to enter a grade for?");
				for (int i = 0; i < list.size(); i++) {
					int index = i + 1;
					System.out.println(index + ") " + list.get(i).toString());
				}
				studentChoice = input.nextInt() - 1;
				badInput = false;
			} catch (InputMismatchException e) {
				//System.out.println(e);
				System.out.println("You must select from the list presented\nusing the index number to choose.");
				input.nextLine();
			} catch (IndexOutOfBoundsException e) {
				//System.out.println(e);
				System.out.println("You must only type a number within range presented.");
				input.nextLine();
			}catch(Exception e) {
				System.out.println(e);
				input.nextLine();
			}
		}while(badInput);
		do {
			badInput = true;
			try {
				System.out.println("Please enter a grade for " + list.get(studentChoice).getName());
				input.nextLine();
				double gradeChange = Double.parseDouble(input.nextLine());
				list.get(studentChoice).setGrade(gradeChange);
				if(0 <= gradeChange && gradeChange <= 100) {
					badInput = false;
				}else {
					System.out.println("You must enter a grade as a number between 0 and 100");
				}
			} catch (NumberFormatException e) {
				System.out.println("You must enter a grade as a number between 0 and 100");
				input.nextLine();
			} catch(Exception e) {
				System.out.println(e);
				input.nextLine();
			}
		} while (badInput);
	}
}

class Student {
	protected String name;
	protected double grade;

	public Student(String name, double grade) {
		this.name = name;
		this.grade = grade;
	}

	public String getName() {
		return name;
	}

	public double getGrade() {
		return grade;
	}

	public void setGrade(double grade) {
		this.grade = grade;
	}

	@Override
	public String toString() {
		return name + " - " + grade;
	}
}
