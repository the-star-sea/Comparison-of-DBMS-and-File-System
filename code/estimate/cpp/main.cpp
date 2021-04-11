#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <ctime>
#include <vector>
#include <cstring>
#include "student.h"
using namespace std;
student stu_vec[4000000];
int leng = sizeof(stu_vec) / sizeof(stu_vec[0]), flag;

int main() {
    ifstream read_in("student.csv");
    string line;
    int i = 0;

    //cout << leng <<endl;
    time_t startTime,endTime;
    startTime = clock();
    while(getline(read_in, line) && i < leng + 1){
        istringstream in(line);
        vector<string> one_line;
        string one_info;
        while (getline(in, one_info, ',')){
            one_line.push_back(one_info);
        }
        if(i == 0){
            i++;
            continue;
        }
        stu_vec[i - 1].id.assign(one_line[0]);
        stu_vec[i - 1].name.assign(one_line[1]);
        stu_vec[i - 1].gender.assign(one_line[2]);
        stu_vec[i - 1].academy.assign(one_line[3]);
        i++;
    }
    endTime = clock();
    cout << "Read total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;
    flag = i;

    search_("计药压");
    search_("M");
    insert_("10000001", "还可以", "F", "树德书院");
    update_("12140348", "11000003");
    delete_("11911611");

    startTime = clock();
    ofstream outFile; // 创建流对象
    outFile.open("new_student.csv", ios::out); // 打开文件
    for (int k = 0; k < flag; k++)
    {
        outFile << stu_vec[k].id << ',';
        outFile << stu_vec[k].name << ',';
        outFile << stu_vec[k].gender << ',';
        outFile << stu_vec[k].academy << ',';
        outFile << endl;
    }
    outFile.close(); // 关闭文件
    endTime = clock();
    cout << "Store total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;

}
void insert_(string id_, string name_, string gender_, string academy_){
    time_t startTime,endTime;
    startTime = clock();
    student temp = {id_, name_, gender_, academy_};
    stu_vec[flag] = temp;
    flag++;
    endTime = clock();
    cout << "Total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;
}

void delete_(string delete_str){
    time_t startTime,endTime;
    startTime = clock();
    int i = 0, pos;
    while(i < leng){
        if(stu_vec[i].id == delete_str){
            pos = i;
            break;
        }
        i++;
    }
    flag--;
    endTime = clock();
    cout << "Total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;
}

void search_(string search_str){
    time_t startTime,endTime;
    startTime = clock();
    int i = 0;
    //cout << "  id     name       gender    college" << endl;
    while(i < leng){
        if(stu_vec[i].id == search_str){
            //cout << stu_vec[i].id << "   " << stu_vec[i].name << "   " << stu_vec[i].gender << "   " << stu_vec[i].academy << endl;
            break;
        }else if(stu_vec[i].name == search_str || stu_vec[i].gender == search_str || stu_vec[i].academy == search_str){
            //cout << stu_vec[i].id << "   " << stu_vec[i].name << "   " << stu_vec[i].gender << "   " << stu_vec[i].academy << endl;
        }
        i++;
    }
    endTime = clock();
    cout << "Total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;
}

void update_(string update_str, string update_pos){
    time_t startTime,endTime;
    startTime = clock();
    int i = 0;
    while (i < leng){
        if (stu_vec[i].id == update_pos){
            stu_vec[i].id = update_str;
            break;
        } else if (stu_vec[i].name == update_pos){
            stu_vec[i].name = update_str;
        }else if (stu_vec[i].gender == update_pos){
            stu_vec[i].gender = update_str;
        }else if (stu_vec[i].academy == update_pos) {
            stu_vec[i].academy = update_str;
        }
        i++;
    }
    endTime = clock();
    cout << "Total Time : "  << double(endTime*1.0 - startTime*1.0)*1.0 << "ms" << endl;
}