package com.ucar.datalink.biz.utils.flinker.check.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang.wang09 on 2018-05-17 15:09.
 */
public class ModifyCheckColumnInfo {

    private List<NameType> nameTypes = new ArrayList<>();

    private List<IndexTye> indexTypes = new ArrayList<>();

    private List<String> names = new ArrayList<>();


    public void addNameType(String name,String type) {
        NameType nt = new NameType();
        nt.name = name;
        nt.type = type;
        nameTypes.add(nt);
    }

    public List<NameType> getNameType() {
        return nameTypes;
    }

    public void addName(String name) {
        names.add(name);
    }

    public List<String> getName() {
        return names;
    }

    public void addIndexType(String index, String type) {
        IndexTye it = new IndexTye();
        it.index = index;
        it.type = type;
        indexTypes.add(it);
    }

    public List<IndexTye> getIndexType() {
        return indexTypes;
    }



    public List<NameType> getList() {
        return nameTypes;
    }

    public boolean compare(ModifyCheckColumnInfo info) {
        if(nameTypes.size() != info.nameTypes.size()) {
            return false;
        }
        //遍历比较
        boolean isEqual = true;
        List<NameType> other = info.getList();
        for(int i=0;i<nameTypes.size();i++) {
            NameType thisName = nameTypes.get(i);
            NameType otherName = other.get(i);
            if( !thisName.equals(otherName) ) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    public class NameType {
        public String name;
        public String type;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NameType nameType = (NameType) o;

            if (!name.equals(nameType.name)) return false;
            return type.equals(nameType.type);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "NameType{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }


    public class IndexTye {
        public String index;
        public String type;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IndexTye indexTye = (IndexTye) o;

            if (!index.equals(indexTye.index)) return false;
            return type.equals(indexTye.type);

        }

        @Override
        public int hashCode() {
            int result = index.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "IndexTye{" +
                    "index='" + index + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }




    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(nameTypes.size() > 0) {
            for (int i = 0; i < nameTypes.size(); i++) {
                sb.append((i + 1)).append("  ").append(nameTypes.get(i)).append("\n");
            }
        }
        else if(names.size() > 0) {
            for (int i = 0; i < names.size(); i++) {
                sb.append((i + 1)).append("  ").append(names.get(i)).append("\n");
            }
        }
        else {
            for (int i = 0; i < indexTypes.size(); i++) {
                sb.append((i + 1)).append("  ").append(indexTypes.get(i)).append("\n");
            }
        }
        return sb.toString();
    }

}
