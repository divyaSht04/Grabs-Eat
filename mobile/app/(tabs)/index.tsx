import React from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/src/hooks/useAuth';

export default function DashboardScreen() {
  const router = useRouter();
  const { user } = useAuth();

  return (
    <View className="flex-1 bg-red-50">
      <ScrollView className="flex-1">
        <View className="bg-primary-600 px-6 py-8 rounded-b-3xl">
          <Text className="text-white text-2xl font-bold mb-2">
            Welcome back, {user?.fullName}! 👋
          </Text>
          <Text className="text-red-100 text-sm">{user?.email}</Text>
        </View>

        <View className="px-6 py-6">
          <Text className="text-gray-800 text-xl font-bold mb-4">Quick Overview</Text>
          
          <View className="flex-row justify-between mb-4">
            <View className="bg-white rounded-xl p-4 flex-1 mr-2 shadow-sm">
              <Text className="text-gray-500 text-xs mb-1">Role</Text>
              <Text className="text-gray-800 text-lg font-bold">{user?.role}</Text>
            </View>
            
            <View className="bg-white rounded-xl p-4 flex-1 ml-2 shadow-sm">
              <Text className="text-gray-500 text-xs mb-1">User ID</Text>
              <Text className="text-gray-800 text-lg font-bold">#{user?.id}</Text>
            </View>
          </View>
        </View>

        <View className="px-6 py-4">
          <Text className="text-gray-800 text-xl font-bold mb-4">Quick Actions</Text>

          <TouchableOpacity 
            className="bg-white rounded-xl p-4 mb-3 flex-row items-center justify-between shadow-sm"
            onPress={() => router.push('/(tabs)/orders')}
          >
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-red-100 rounded-full items-center justify-center mr-3">
                <Text className="text-xl">📋</Text>
              </View>
              <Text className="text-gray-700 font-medium">My Orders</Text>
            </View>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            className="bg-white rounded-xl p-4 mb-3 flex-row items-center justify-between shadow-sm"
            onPress={() => router.push('/(tabs)/menu')}
          >
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-red-100 rounded-full items-center justify-center mr-3">
                <Text className="text-xl">🍽️</Text>
              </View>
              <Text className="text-gray-700 font-medium">Browse Menu</Text>
            </View>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            className="bg-white rounded-xl p-4 mb-3 flex-row items-center justify-between shadow-sm"
            onPress={() => router.push('/(tabs)/profile')}
          >
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-red-100 rounded-full items-center justify-center mr-3">
                <Text className="text-xl">👤</Text>
              </View>
              <Text className="text-gray-700 font-medium">My Profile</Text>
            </View>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}
