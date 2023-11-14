var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI')

function initializeCoreMod() {
    return {
        'biome': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.biome.Biome' 
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'climateSettings', 'getModifiedClimateSettings')
                ASMAPI.redirectFieldToMethod(classNode, 'specialEffects', 'getModifiedSpecialEffects')
                return classNode;
            }
        },
        'structure': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.levelgen.structure.Structure'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'settings', 'getModifiedStructureSettings')
                return classNode;
            }
        },
        'potion': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.effect.MobEffectInstance'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'effect', 'getEffect') // potion
                return classNode;
            }
        },
        'flowing_fluid_block': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.LiquidBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'fluid', 'getFluid')
                return classNode;
            }
        },
        'bucketitem': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.BucketItem'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'content', 'getFluid')
                return classNode;
            }
        },
        'stairsblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.StairBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'base', 'getModelBlock')
                ASMAPI.redirectFieldToMethod(classNode, 'baseState', 'getModelState')
                return classNode;
            }
        },
        'flowerpotblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.FlowerPotBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'content', 'getContent') // flower
                return classNode;
            }
        },
        'itemstack': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.ItemStack'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'item', 'getItem') // item
                return classNode;
            }
        }
    }
}
