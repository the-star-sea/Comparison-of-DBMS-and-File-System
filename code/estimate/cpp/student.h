#ifndef PRO1_TASK3_STUDENT_H
#define PRO1_TASK3_STUDENT_H
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct student{
    string id;
    string name;
    string gender;
    string academy;
    /*char id[9];
    char name[7];
    char gender[2];
    char academy[9];*/
};
void insert_(string id_, string name_, string gender_, string academy_);
void delete_(string delete_str);
void search_(string search_str);
void update_(string update_str, string update_pos);

#endif //PRO1_TASK3_STUDENT_H
