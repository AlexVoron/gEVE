package test1

/**
 * Created by a.n.vorotnikov on 18.02.2015.
 */

class Cl {
    def methodMissing(String name, args) {
        println "method=$name"
    }
}

//def asd = ['2': {dummy -> 'qwe'}]
//def asd = new Cl()
//println asd.2('zxc')
ExpandoMetaClass.enableGlobally()
Number.metaClass.unit = {type -> new MaterialSlot(type: type, quantity: delegate) }
Number.metaClass.units = {type -> new MaterialSlot(type: type, quantity: delegate) }

def tritanium = new MaterialType(name: "Tritanium", typeId: 34, price: 6)
def pyerite = new MaterialType(name: "Pyerite", typeId: 35, price: 12.37)

println (1.unit (tritanium))
println (2.units (pyerite))
