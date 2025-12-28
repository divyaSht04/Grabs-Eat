import React from 'react';
import { View, Text, ScrollView } from 'react-native';

export default function MenuScreen() {
  return (
    <View className="flex-1 bg-red-50">
      <ScrollView className="flex-1 px-6 py-6">
        <View className="bg-white rounded-2xl p-6 shadow-sm mb-4">
          <Text className="text-2xl font-bold text-gray-800 mb-2">Menu</Text>
          <Text className="text-gray-600">Browse our delicious offerings.</Text>
        </View>

        <View className="bg-white rounded-2xl p-6 shadow-sm">
          <Text className="text-gray-500 text-center py-8">
            🍽️ Menu items coming soon
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}
