package test1

/**
 * Created by a.n.vorotnikov on 18.02.2015.
 */

class Cl {
    def invokeMethod(String name, args) {
        println "method=$name"
    }
}

//def asd = ['2': {dummy -> 'qwe'}]
def asd = new Cl()
println asd.2('zxc')

